package io.github.rohergun.budgetmanager.exception;

import lombok.Getter;


@Getter
public class BudgetManagerException extends RuntimeException{

    private final DomainErrorMessage errorMessage;

    public BudgetManagerException(DomainErrorMessage errorMessage) {
        super(errorMessage.getDescription());
        this.errorMessage = errorMessage;
    }
}
