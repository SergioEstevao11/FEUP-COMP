import org.junit.Test;
import pt.up.fe.comp.TestUtils;

public class AnalyseTest {
    @Test
    public void test(){
        var results = TestUtils.analyse("cp2lecture/HelloWorld.jmm");
        System.out.println("Symbol Table: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }
}
