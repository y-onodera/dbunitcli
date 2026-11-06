package yo.dbunitcli.resource.poi.jxls;

import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jxls.expression.EvaluationException;
import org.jxls.expression.ExpressionEvaluator;
import org.jxls.expression.ExpressionEvaluatorFactory;
import yo.dbunitcli.resource.jexl.JexlEngines;

import java.util.Map;

/**
 * テンプレートの式を {@link JexlEngines#TEMPLATE} で評価する。
 * jxls の JexlExpressionEvaluator は式の解析結果をエンジンを問わず式文字列だけで共有キャッシュするため、継承せず実装する。
 */
public class JexlFunctionsEvaluatorFactory implements ExpressionEvaluatorFactory {

    @Override
    public ExpressionEvaluator createExpressionEvaluator(final String expression) {
        return new Evaluator(expression == null ? null : JexlEngines.TEMPLATE.createExpression(expression));
    }

    private record Evaluator(JexlExpression expression) implements ExpressionEvaluator {

        @Override
        public Object evaluate(final String expression, final Map<String, Object> context) {
            return Evaluator.evaluate(JexlEngines.TEMPLATE.createExpression(expression), context);
        }

        @Override
        public Object evaluate(final Map<String, Object> context) {
            return Evaluator.evaluate(this.expression, context);
        }

        @Override
        public String getExpression() {
            return this.expression == null ? null : this.expression.getSourceText();
        }

        private static Object evaluate(final JexlExpression expression, final Map<String, Object> context) {
            try {
                return expression.evaluate(new MapContext(context));
            } catch (final Exception e) {
                throw new EvaluationException("An error occurred when evaluating expression " + expression.getSourceText(), e);
            }
        }
    }
}
