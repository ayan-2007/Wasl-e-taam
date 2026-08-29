package com.waslataam.command;

import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandInvoker {
    private static final Logger log = LoggerFactory.getLogger(CommandInvoker.class);
    private final Deque<DonationCommand> history = new ArrayDeque<>();
    private static final int MAX_HISTORY = 100;

    public void execute(DonationCommand command) throws IllegalStateTransitionException {
        log.debug("Executing command: {}", command.describe());
        command.execute();
        history.addLast(command);
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    public void undo() throws IllegalStateTransitionException {
        DonationCommand lastCommand = history.pollLast();
        if (lastCommand == null) {
            log.warn("No commands to undo");
            return;
        }
        log.debug("Undoing command: {}", lastCommand.describe());
        lastCommand.undo();
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public int getHistorySize() {
        return history.size();
    }

    public DonationCommand peekLast() {
        return history.peekLast();
    }
}