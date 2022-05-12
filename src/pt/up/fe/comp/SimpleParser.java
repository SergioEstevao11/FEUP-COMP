package pt.up.fe.comp;

import java.util.Collections;
import java.util.Map;

import pt.up.fe.comp.ast.LineColAnnotator;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class SimpleParser implements JmmParser {

    /*
    CÓDIGO STOR

    @Override
    public JmmParserResult parse(String jmmCode, Map<String,String> config){
        return parse(jmmCode, "Program",config);
    }
    @Override
    public JmmParserResult parse(String jmmCode,String startingNode, Map<String, String> config) {
        try{
            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));
            var root = parser.rootNode();

            if(root == null){
                throw new ParseException(parser, "Parsing Problem, root is null");
            }

            new LineColAnnotator().visit((JmmNode) root);

            //var baseNode = (BaseNode) root;
            //baseNode.getBeginLine();

            if (!(root instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }

            return new JmmParserResult((JmmNode) root, Collections.emptyList(), config);

        } catch (ParseException e){
            Token t = e.getToken();
            int line = t.getBeginLine();
            int column = t.getBeginColumn();
            String message = e.getMessage();
            Report report = Report.newError(Stage.SYNTATIC, line, column, message, e);
            return JmmParserResult.newError(report);
        }
            catch (Exception e) {
            String message = e.getMessage();
            return JmmParserResult.newError(Report.newError(Stage.OTHER, -1, -1, message, e));
        }
    }*/


    //CÓDIGO DO SÉRGIO
    @Override
    public JmmParserResult parse(String jmmCode, Map<String, String> config) {

        try {
            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));
            parser.Start();

            Node root = parser.rootNode();
            root.dump("");

            if (!(root instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }

            return new JmmParserResult((JmmNode) root, Collections.emptyList(), config);

        } catch (ParseException e) {
            Token t = e.getToken();
            int line = t.getBeginLine();
            int column = t.getBeginColumn();
            String message = e.getMessage();
            Report report = Report.newError(Stage.SYNTATIC, line, column, message, e);
            return JmmParserResult.newError(report);
        } catch (Exception e) {
            String message = e.getMessage();
            return JmmParserResult.newError(Report.newError(Stage.OTHER, -1, -1, message, e));
        }
    }
}
