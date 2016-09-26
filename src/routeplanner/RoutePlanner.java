package routeplanner;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.apache.commons.collections15.Transformer;


class Ruta{
    private ArrayList<Tienda> ruta = null;
    
    public Ruta(){
        
    }
    
    public Ruta(ArrayList<Tienda> ruta){
        this.ruta = ruta;
    }
    
    public void setRuta(ArrayList<Tienda> ruta){
        this.ruta = ruta;
    }
    
    public ArrayList<Tienda> getRuta(){
        return this.ruta;
    }
}

class Tienda{
    
    private int x_position;
    private int y_position;
    private String storeName;
    private boolean isDistributor;
            
    public Tienda(){
        
    }
    
    public Tienda(int x_position, int y_position, boolean isDistributor, String storeName){
        this.x_position = x_position;
        this.y_position = y_position;
        this.isDistributor = isDistributor;
        this.storeName = storeName;
    }
    
    public void setXPosition(int x_position){
        this.x_position = x_position;
    }
    
    public int getXPosition(){
        return this.x_position;
    }
    
    public void setYPosition(int y_position){
        this.y_position = y_position;
    }
    
    public int getYPosition(){
        return this.y_position;
    }
    
    public void setDistributor(boolean isDistributor){
        this.isDistributor = isDistributor;
    }
    
    public boolean isDistributor(){
        return this.isDistributor;
    }
    
    public void setStoreName(String storeName){
        this.storeName = storeName;
    }
    
    public String getStoreName(){
        return this.storeName;
    }
}

public class RoutePlanner {
    
    static int numero_camiones;
    static Tienda centro_distribucion = new Tienda();
    static ArrayList<Tienda> tiendas = new ArrayList();
    
    public static double calcularDistancia(double x1, double x2, double y1, double y2){
        return Math.sqrt((Math.pow((x2-x1), 2)) + (Math.pow(y2-y1,2)));
    }
    
    public static void main(String[] args) {
        /** Leer la informacion del archivo y guardarla en un arreglo para poder utilizarla **/
        try{
            FileReader filereader = new FileReader("input.txt");
            BufferedReader buffer = new BufferedReader(filereader);
            String line = new String();
            int linenumber = 0, storenumber = 1;
            while( (line = buffer.readLine()) != null){
                if(linenumber == 0){
                    numero_camiones = Integer.parseInt(line);
                }
                if(linenumber == 1){
                    String[] coordenates = line.split(",");
                    centro_distribucion.setXPosition(Integer.parseInt(coordenates[0]));
                    centro_distribucion.setYPosition(Integer.parseInt(coordenates[1]));
                    centro_distribucion.setDistributor(true);
                    centro_distribucion.setStoreName("Centro de Distribucion");
                }
                if(linenumber > 1){
                    Tienda tienda = new Tienda();
                    String[] coordenates = line.split(",");
                    tienda.setXPosition(Integer.parseInt(coordenates[0]));
                    tienda.setYPosition(Integer.parseInt(coordenates[1]));
                    tienda.setDistributor(false);
                    tienda.setStoreName("Tienda #"+storenumber);
                    tiendas.add(tienda);
                    storenumber++;
                }
                linenumber++;
            }
                       
        }catch(Exception e){
            
        }
        
        /* Creamos el grafo que usaremos para visualizar el mapa */
        Graph<String, Double> grafo = new SparseMultigraph<String, Double>();
        grafo.addVertex(centro_distribucion.getStoreName());
        for(int i = 0; i < tiendas.size(); i++){
            grafo.addVertex(tiendas.get(i).getStoreName());
        }
        
        /*Calcular la distancia de todas las tiendas hacia el punto de distribucion */
        
        for(int i = 0; i < tiendas.size(); i++){
            double distance = 0;
            double firstTerm = Math.pow((tiendas.get(i).getXPosition()-centro_distribucion.getXPosition()),2);
            double secondTerm = Math.pow((tiendas.get(i).getYPosition()-centro_distribucion.getYPosition()),2);
            distance = Math.sqrt(firstTerm+secondTerm);
            grafo.addEdge(distance, tiendas.get(i).getStoreName(), centro_distribucion.getStoreName(), EdgeType.UNDIRECTED);
        }
          
        /* Calcular la distancia de todas las tiendas a todas las tiendas, esto para
            saber el peso de las aristas y luego las agregamos al grafo */
        
        for(int i = 0; i < tiendas.size(); i++){
            for(int j = i+1; j < tiendas.size(); j++){
                double distance = 0.0;
                double firstTerm = Math.pow((tiendas.get(j).getXPosition()-tiendas.get(i).getXPosition()),2);
                double secondTerm = Math.pow((tiendas.get(j).getYPosition())-tiendas.get(i).getYPosition(), 2);
                distance = Math.sqrt(firstTerm+secondTerm);
                grafo.addEdge(distance, tiendas.get(i).getStoreName(), tiendas.get(j).getStoreName(), EdgeType.UNDIRECTED);
            }
        }
        
        /*Metodos de la libreria JUNG para poder visualizar el mapa */
        Layout<String, Double> layout = new CircleLayout(grafo);
        layout.setSize(new Dimension(400,400)); 
        
        BasicVisualizationServer<String,Double> vv =
              new BasicVisualizationServer<String,Double>(layout);
        vv.setPreferredSize(new Dimension(500,500)); 
        
        Transformer<String,Paint> vertexPaint = new Transformer<String,Paint>() {
            public Paint transform(String i) {
                return Color.GREEN;
} };
        float dash[] = {10.0f};
        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
             BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        Transformer<Double, Stroke> edgeStrokeTransformer =
              new Transformer<Double, Stroke>() {
            public Stroke transform(Double s) {
                return edgeStroke;
            }
        };
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        JFrame frame = new JFrame("Pepsi Co");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
        
        /*Resolucion del Problema */
        
        try{
            ArrayList<Ruta> rutas = new ArrayList<Ruta>();
            FileWriter filewriter = new FileWriter("./salida.txt");
            BufferedWriter bufferWriter = new BufferedWriter(filewriter);
            
            int k = 0;
            while(k < numero_camiones){
                double totalDistance = 0;
                double minimumdistance = 0;
                Ruta ruta = new Ruta();
                ArrayList<Tienda> tiendasEnRuta = new ArrayList<Tienda>();
                tiendasEnRuta.add(centro_distribucion);
                for(int i = 0; i < tiendas.size(); i++){
                    for(int j = i+1; j < tiendas.size(); j++){
                        if(minimumdistance <= calcularDistancia(tiendas.get(j).getXPosition(),tiendas.get(i).getXPosition(), tiendas.get(j).getYPosition(), tiendas.get(i).getYPosition())){
                            tiendasEnRuta.add(tiendas.get(j));
                            minimumdistance = calcularDistancia(tiendas.get(j).getXPosition(),tiendas.get(i).getXPosition(), tiendas.get(j).getYPosition(), tiendas.get(i).getYPosition());
                            totalDistance += 0;
                            tiendas.remove(j);
                        }  
                    }
                }
                ruta.setRuta(tiendasEnRuta);
                rutas.add(ruta);
                k++;
            }
            
            for(int i = 0; i < rutas.size(); i++){
                for(int j = 0; j < rutas.get(i).getRuta().size(); j++){
                    System.out.println(rutas.get(i).getRuta().get(j).getStoreName());
                }
            }
            
            
        }catch(Exception e){
            
        }
        
        
        

        
    }
    
}
