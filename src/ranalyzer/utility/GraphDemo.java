package ranalyzer.utility;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import javax.swing.*;
import java.awt.*;

public class GraphDemo extends JApplet {
    private static final long serialVersionUID = -5345319851341875800L;
    private Graph<String, String> g = null;
    private VisualizationViewer<String, String> vv = null;
    private AbstractLayout<String, String> layout = null;

    public void init(Graph<String, String> g) {
        this.g = g;
        this.layout = new CircleLayout(this.g);
        this.vv = new VisualizationViewer(this.layout, new Dimension(1280, 720));
        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", true);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.lightGray);
        this.getContentPane().setFont(new Font("Serif", 0, 12));
        this.vv.setGraphMouse(new DefaultModalGraphMouse());
        this.vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
        this.vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        this.vv.setForeground(Color.black);
        this.getContentPane().add(this.vv);
    }
}
