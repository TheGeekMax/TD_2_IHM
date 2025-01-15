package ensisa.lines.tools;

import ensisa.lines.MainController;
import ensisa.lines.model.StraightLine;
import javafx.scene.input.MouseEvent;

public class SelectTool implements Tool {
    private State state;
    private MainController mainController;
    private StraightLine straightLine;
    private double lastX;
    private double lastY;

    public SelectTool(MainController mainController) {
        this.mainController = mainController;
        state = State.initial;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            state = State.selection;
            lastX = event.getX();
            lastY = event.getY();
            straightLine = mainController.findLineForPoint(event.getX(), event.getY());
            if (straightLine != null) {
                var isSelected = mainController.getSelectedLines().contains(straightLine);
                if (isSelected && mainController.getLinesEditor().
                        isPointInStartSelectionSquare(event.getX(), event.getY(), straightLine)) {
                    state = State.selectionAtStart;
                } else if (isSelected && mainController.getLinesEditor().
                        isPointInEndSelectionSquare(event.getX(), event.getY(), straightLine)) {
                    state = State.selectionAtEnd;
                } else {
                    if (!event.isShiftDown()) {
                        if (!isSelected)
                            mainController.selectLine(straightLine, false);
                    } else if (isSelected)
                        mainController.deselectLine(straightLine);
                    else
                        mainController.selectLine(straightLine, true);
                }
            } else {
                if (!event.isShiftDown())
                    mainController.deselectAll();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            double deltaX = event.getX() - lastX;
            double deltaY = event.getY() - lastY;
            switch (state) {
                case selection:
                    for (var line : mainController.getSelectedLines()) {
                        line.offset(deltaX, deltaY);
                    }
                    break;
                case selectionAtStart: {
                    straightLine.setStartX(straightLine.getStartX() + deltaX);
                    straightLine.setStartY(straightLine.getStartY() + deltaY);
                }
                break;
                case selectionAtEnd: {
                    straightLine.setEndX(straightLine.getEndX() + deltaX);
                    straightLine.setEndY(straightLine.getEndY() + deltaY);
                }
                break;
            }
            lastX = event.getX();
            lastY = event.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        state = State.initial;
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        Tool.super.mouseEntered(event);
    }

    @Override
    public void mouseExited(MouseEvent event) {
        Tool.super.mouseExited(event);
    }

    enum State {initial, selection, selectionAtStart, selectionAtEnd}
}
