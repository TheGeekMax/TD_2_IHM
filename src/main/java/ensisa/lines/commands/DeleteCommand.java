package ensisa.lines.commands;

import ensisa.lines.MainController;
import ensisa.lines.model.StraightLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeleteCommand implements UndoableCommand {
    private MainController mainController;
    private List<StraightLine> savedLines;
    private Set<StraightLine> savedSelectedLines;

    public DeleteCommand(MainController mainController) {
        this.mainController = mainController;
        savedLines = new ArrayList<>(mainController.getDocument().getLines());
        savedSelectedLines = new HashSet<>(mainController.getSelectedLines());
    }


    @Override
    public void undo() {
        mainController.deselectAll();
        mainController.getDocument().getLines().clear();
        mainController.getDocument().getLines().addAll(savedLines);
        mainController.getSelectedLines().addAll(savedSelectedLines);
    }

    @Override
    public void execute() {
        mainController.deselectAll();
        mainController.getDocument().getLines().removeAll(savedSelectedLines);
    }
}
