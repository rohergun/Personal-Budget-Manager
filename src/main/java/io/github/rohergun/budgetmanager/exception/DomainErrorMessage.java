package io.github.rohergun.budgetmanager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainErrorMessage {

    // User domain
    USER_NOT_F0UND(HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email is already exists"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),

    // Category domain
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),

    // Budget domain
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "Budget not found"),
    BUDGET_ALREADY_EXISTS(HttpStatus.CONFLICT, "A budget for this category already exists"),

    // Transaction domain
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),

    // Financial goal domain
    FINANCIAL_GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Financial goal not found");


    private final HttpStatus httpStatus;
    private final String description;

    DomainErrorMessage(HttpStatus httpStatus, String description) {
        this.httpStatus = httpStatus;
        this.description = description;
    }
}
