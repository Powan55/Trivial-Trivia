package com.example.Command;

/**
 * The Action interface represents an action that can be executed.
 * Implementations of this interface encapsulate specific actions
 * to be performed in response to various events or triggers.
 */
public interface Action {

    /**
     * Executes the action.
     * This method should perform the specific action associated
     * with the implementing class.
     */
    public void execute();
}
