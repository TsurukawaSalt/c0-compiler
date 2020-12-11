public enum ErrorType {
    IllegalInput("Illegal input error!"),
    OpenFile("Fail to open file!"),
    CloseFile("Fail to close file!"),
    ExpectedToken("ExpectedToken: "),
    NoIfKeyWord("No if keyword"),
    NoWhileKeyWord("No while keyword"),
    NoBreakKeyWord("No break keyword"),
    NoContinueKeyWord("No continue keyword"),
    NoReturnKeyWord("No return keyword"),
    NoConstKeyWord("No const keyword"),
    NoMainFunction("No main function"),
    NoReturn("No return"),
    NotDeclared("Not declared"),
    DuplicateDeclaration("Duplicate declaration"),
    InvalidToken("Invalid token"),
    InvalidReturn("Invalid return"),
    InvalidParams("Invalid params"),
    InvalidAssignment("Invalid assignment"),
    InvalidAs("Invalid as"),
    InvalidCalculation("Invalid calculation"),
    InvalidContinue("Invalid continue"),
    InvalidBreak("Invalid break"),
    InvalidType("Invalid type"),
    InvalidOperator("Invalid operator"),
    WrongReturn("Wrong return form"),
    OtherError("Other error:");

    private String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
