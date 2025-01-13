package ensisa.lines.tools;

import ensisa.lines.MainController;
import javafx.scene.input.MouseEvent;

public class SelectTool implements Tool {
    private MainController mainController;

    public SelectTool(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Tool.super.mousePressed(event);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        Tool.super.mouseDragged(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        Tool.super.mouseReleased(event);
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        Tool.super.mouseEntered(event);
    }

    @Override
    public void mouseExited(MouseEvent event) {
        Tool.super.mouseExited(event);
    }
}
