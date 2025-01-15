package ensisa.lines;

import ensisa.lines.commands.DeleteCommand;
import ensisa.lines.commands.UndoableCommand;
import ensisa.lines.model.Document;
import ensisa.lines.model.StraightLine;
import ensisa.lines.tools.DrawTool;
import ensisa.lines.tools.SelectTool;
import ensisa.lines.tools.Tool;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MainController {
    private final Document document;
    private final ObservableSet<StraightLine> selectedLines;
    private final ObjectProperty<Tool> currentTool;
    private final DrawTool drawTool;
    private final SelectTool selectTool;
    private final UndoRedoHistory undoRedoHistory;
    @FXML
    public Pane editorPane;
    private LinesEditor linesEditor;
    @FXML
    private RadioButton selectToolButton;
    @FXML
    private RadioButton drawToolButton;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private TextField lineWidthTextField;
    @FXML
    private ColorPicker colorPicker;

    public MainController() {
        this.document = new Document();
        selectTool = new SelectTool(this);
        drawTool = new DrawTool(this);
        currentTool = new SimpleObjectProperty<>(selectTool);
        selectedLines = FXCollections.observableSet();
        undoRedoHistory = new UndoRedoHistory();
    }

    public void initialize() {
        linesEditor = new LinesEditor(editorPane);
        setClipping();
        initializeToolPalette();
        initializeMenus();
        initializeInspector();
        observeDocument();
        observeSelection();
    }

    private void initializeInspector() {
        selectedLines.addListener(new SetChangeListener<StraightLine>() {
            @Override
            public void onChanged(Change<? extends StraightLine> change) {
                lineWidthTextField.setText(findCommonStrokeWidth());
                colorPicker.setValue(findCommonColor());
            }

            private Color findCommonColor() {
                boolean foundOne = false;
                Color color = Color.TRANSPARENT;
                for (var l : selectedLines) {
                    if (!foundOne) {
                        color = l.getColor();
                        foundOne = true;
                    } else {
                        if (!color.equals(l.getColor()))
                            return Color.TRANSPARENT;
                    }
                }
                return foundOne ? color : Color.TRANSPARENT;
            }

            private String findCommonStrokeWidth() {
                boolean foundOne = false;
                double width = 0.0;
                for (var l : selectedLines) {
                    if (!foundOne) {
                        width = l.getStrokeWidth();
                        foundOne = true;
                    } else {
                        if (width != l.getStrokeWidth())
                            return "";
                    }
                }
                return foundOne ? String.valueOf(width) : "";
            }
        });
    }

    private void initializeMenus() {
        undoMenuItem.disableProperty().bind(undoRedoHistory.canUndoProperty().not());
        redoMenuItem.disableProperty().bind(undoRedoHistory.canRedoProperty().not());
        deleteMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> selectedLines.isEmpty(), selectedLines));
    }

    private void observeSelection() {
        selectedLines.addListener(new SetChangeListener<StraightLine>() {
            @Override
            public void onChanged(Change<? extends StraightLine> change) {
                if (change.wasRemoved()) {
                    linesEditor.deselectLine(change.getElementRemoved());
                }
                if (change.wasAdded()) {
                    linesEditor.selectLine(change.getElementAdded());
                }
            }
        });
    }

    private void initializeToolPalette() {
        // Change style class to not paint the round button
        selectToolButton.getStyleClass().remove("radio-button");
        selectToolButton.getStyleClass().add("toggle-button");
        drawToolButton.getStyleClass().remove("radio-button");
        drawToolButton.getStyleClass().add("toggle-button");
    }

    private void setClipping() {
        final Rectangle clip = new Rectangle();
        editorPane.setClip(clip);
        editorPane.layoutBoundsProperty().addListener((v, oldValue, newValue) -> {
            clip.setWidth(newValue.getWidth());
            clip.setHeight(newValue.getHeight());
        });
    }

    private void observeDocument() {
        document.getLines().addListener(new ListChangeListener<StraightLine>() {
            public void onChanged(ListChangeListener.Change<? extends StraightLine> c) {
                while (c.next()) {
                    // Des lignes ont été supprimées du modèle
                    for (StraightLine line : c.getRemoved()) {
                        deselectLine(line);
                        linesEditor.removeLine(line);
                    }
                    // Des lignes ont été ajoutées au modèle
                    for (StraightLine line : c.getAddedSubList()) {
                        linesEditor.createLine(line);
                    }
                }
            }
        });
    }

    //tools
    @FXML
    private void selectToolAction() {
        System.out.println("Select tool");
        setCurrentTool(selectTool);
    }

    @FXML
    private void drawToolAction() {
        System.out.println("Draw tool");
        setCurrentTool(drawTool);
    }

    //events handler
    @FXML
    private void mousePressedInEditor(MouseEvent event) {
        getCurrentTool().mousePressed(event);
    }

    @FXML
    private void mouseDraggedInEditor(MouseEvent event) {
        getCurrentTool().mouseDragged(event);
    }

    @FXML
    private void mouseReleasedInEditor(MouseEvent event) {
        getCurrentTool().mouseReleased(event);
    }

    @FXML
    private void mouseEntered(MouseEvent event) {
        getCurrentTool().mouseEntered(event);
    }

    @FXML
    void mouseExited(MouseEvent event) {
        getCurrentTool().mouseExited(event);
    }

    //for tool handling

    public ObjectProperty<Tool> currentToolProperty() {
        return currentTool;
    }

    public Tool getCurrentTool() {
        return currentTool.get();
    }

    public void setCurrentTool(Tool currentTool) {
        this.currentTool.set(currentTool);
    }

    public ObservableSet<StraightLine> getSelectedLines() {
        return selectedLines;
    }

    public void selectLine(StraightLine line, boolean keepSelection) {
        if (!keepSelection)
            getSelectedLines().clear();
        getSelectedLines().add(line);
    }

    public void deselectLine(StraightLine line) {
        getSelectedLines().remove(line);
    }

    public void deselectAll() {
        getSelectedLines().clear();
    }

    public StraightLine findLineForPoint(double x, double y) {
        for (var straightLine : getDocument().getLines()) {
            if (linesEditor.isPointInStartSelectionSquare(x, y, straightLine) ||
                    linesEditor.isPointInEndSelectionSquare(x, y, straightLine) ||
                    linesEditor.isPointInLine(x, y, straightLine))
                return straightLine;
        }
        return null;
    }

    //for commands handling
    public void execute(UndoableCommand command) {
        undoRedoHistory.execute(command);
    }

    @FXML
    private void undoMenuItemAction() {
        undoRedoHistory.undo();
    }

    @FXML
    private void redoMenuItemAction() {
        undoRedoHistory.redo();
    }

    @FXML
    private void deleteMenuItemAction() {
        undoRedoHistory.execute(new DeleteCommand(this));
    }

    //for inspector handling
    @FXML
    private void lineWidthTextFieldAction() {
        System.out.println("Line width changed");
        try {
            var value = Double.parseDouble(lineWidthTextField.getText());
            if (value >= 1.0) {
                selectedLines.forEach(straightLine -> {
                    straightLine.setStrokeWidth(value);
                });
            }
        } catch (NumberFormatException ex) {
        }
    }

    @FXML
    private void colorPickerAction() {
        var color = colorPicker.getValue();
        selectedLines.forEach(straightLine -> {
            straightLine.setColor(color);
        });
    }

    @FXML
    private void quitMenuAction() {
        Platform.exit();
    }

    public Document getDocument() {
        return document;
    }

    public LinesEditor getLinesEditor() {
        return linesEditor;
    }
}