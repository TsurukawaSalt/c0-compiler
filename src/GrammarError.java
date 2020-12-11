/**
 * GrammarError
 */
public class GrammarError extends RuntimeException{
    private Pos position;
    private ErrorType errorType;
    private String info;

    public GrammarError(ErrorType errorType, Pos position, String info){
        this.errorType = errorType;
        this.position = position;
        this.info = info;
    }

    public GrammarError(ErrorType errorType, Pos position){
        this.errorType = errorType;
        this.position = position;
        this.info = "";
    }

    public GrammarError(ErrorType errorType, String info){
        this.errorType = errorType;
        this.position = new Pos(0,0);
        this.info = info;
    }

    public GrammarError(ErrorType errorType){
        this.errorType = errorType;
        this.position = new Pos(0,0);
        this.info = "";
    }

    public String toString(){
        return errorType.getMessage() + info;
    }
}
