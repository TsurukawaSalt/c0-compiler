/**
 * TokenError
 */
public class TokenError extends RuntimeException{

    private ErrorType errorType;
    private Pos position;
    private String info;

    public TokenError(ErrorType errorType, Pos position, String info){
        this.errorType = errorType;
        this.position = position;
        this.info = info;
    }

    public TokenError(ErrorType errorType, Pos position){
        this.errorType = errorType;
        this.position = position;
        this.info = "";
    }

    public TokenError(ErrorType errorType){
        this.errorType = errorType;
        this.position = new Pos(0,0);
    }

    @Override
    public String toString(){
        return "ERROR: Row\t" + position.row + ", column\t" + position.col + ": " + errorType.getMessage() + info;
    }
}
