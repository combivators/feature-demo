package net.tiny.feature.assess;

import java.util.ArrayList;
import java.util.function.BiFunction;

/**
 * Appraiser : 根据评价策略表对资历数据进行评估
 * 资历数据来源于Digitalization的数值化处理
 */
public class Appraiser implements BiFunction<Form, Assets, Assessment> {

    /** 计算公式:各项评分乘权重的合计 sum(score*weight) */
    private BiFunction<Double[], Double[], Double> formula =
        new BiFunction<Double[], Double[], Double>() {
            @Override
            public Double apply(Double[] t, Double[] u) {
                Double ret = 0.0d;
                for (int i = 0; i < t.length; i++) {
                    ret += t[i] * u[i];
                }
                return ret;
            }
    };

    public BiFunction<Double[], Double[], Double> getFormula() {
        return formula;
    }

    public Appraiser setFormula(BiFunction<Double[], Double[], Double> fun) {
        formula = fun;
        return this;
    }

    @Override
    public Assessment apply(Form form, Assets assets) {
        return assess(form.toTables(assets.titles()), assets.toQualifications());
    }

    public Assessment assess(Form.Tables tables, Assets.Qualifications qualifications) {
        final Assessment assessment = new Assessment();
        assessment.id = tables.id.hashCode()*3 + qualifications.id.hashCode()*7;
        assessment.form = tables.id;
        assessment.assets = qualifications.id;
        assessment.signature = tables.signature + ":" + qualifications.signature;
        assessment.scores = new ArrayList<>();

        final Matrix<Double> matrix = tables.matrix();
        Double[] res = matrix.computeColumns(formula, qualifications.scores);
        boolean typed = (null != tables.types && res.length == tables.types.size());

        for (int i = 0; i < res.length; i++) {
            Assessment.Score score = new Assessment.Score();
            if (typed) {
                score.type = tables.types.get(i);
            } else {
                score.type = "";
            }
            score.value = res[i];
            assessment.scores.add(score);
        }
        return assessment;
    }

    public Assessment.Scores score(Form.Tables tables, Assets.Qualifications qualifications) {
        return assess(tables, qualifications).toScores();
    }
}
