package src.BuildingBlocks.Errors;

public class ExlError {
    private int line ;
    private String reason ;

    public ExlError(int l , String r ){
        line = l ;
        reason = r ;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "There was and error on line " + line + " that states " + reason;
    }
}
