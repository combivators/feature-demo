package net.tiny.feature.assess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

import net.tiny.feature.assess.Assessment.Score;

/**
 * 应用层评价项目和分类
 */
public final class Types {

/*
    public static enum Catalog {
        //技术才能
        Ability,
        //社会
        Community,
        //自然人性
        Nature
    }
*/
    public static enum Title {
        Id,
        Sex,
        Age,
        Nationality,
        Civil,
        Visa,
        Education,
        Specialty,
        Graduation,
        Income,
        Certification,
        Years,
        Changes,
        Career,
        Experience,
        Duties,
        Field,
        Language;
    }

    public static enum Type {
        //技能
        Skill(1, "Skill", "Ability"),
        //专业学知
        Learning(2, "Learning", "Ability"),
        //管理能力
        Management(3, "Management", "Community"),
        //生产效率
        Productivity(4, "Productivity", "Community"),
        //发展
        Growing(5, "Growing", "Nature"),
        //容和性
        Compatibility(6, "Compatibility", "Nature");

        public final int value;
        public final String name;
        public final String catalog;

        Type(int v, String n, String c) {
            this.value = v;
            this.name = n;
            this.catalog = c;
        }

        private static Map<Integer, Type> map = new HashMap<>();
        static {
            for(Type t : Type.values()){
                map.put(t.value, t);
            }
        }
        public int value() {
            return this.value;
        }
        public static Type valueOf(int val) {
            return map.get(val);
        }

        public static Type[] types(String c) {
            Type[] types = new Type[2];
            switch(c) {
            case "Ability":
                types = new Type[] {Skill, Learning};
                break;
            case "Community":
                types = new Type[] {Management, Productivity};
                break;
            case "Nature":
                types = new Type[] {Growing, Compatibility};
                break;
            default:
                break;
            }
            return types;
        }
    }

    private static final List<String> TYPE_NAMES = new ArrayList<>();
    static {
        for(Type t : Type.values()) {
            TYPE_NAMES.add(t.name());
        }
        Collections.unmodifiableList(TYPE_NAMES);
    }

    private static final Function<Score, String> CATALOG_FUNC = new Function<Score, String>() {
        @Override
        public String apply(Score s) {
            return Type.valueOf(s.type).catalog;
        }
    };

    public static List<String> getTypeNames() {
        return TYPE_NAMES;
    }
    public static Function<Score, String> getCatalog() {
        return CATALOG_FUNC;
    }

    public static int[] getTitleOrders(String[] titles) {
        int[] orders = new int[titles.length];
        for (int i=0; i<orders.length; i++) {
            orders[i] = Title.valueOf(titles[i]).ordinal();
        }
        return orders;
    }

    public static IntFunction<String> getTypeFunction() {
        return new IntFunction<String> () {
            @Override
            public String apply(int value) {
                try {
                    return Type.valueOf(value).name;
                } catch (Exception e) {
                    return "Unknow";
                }
            }
        };
    }

    public static Form.Policy generatePolicy(String titel, double[] values) {
        IntFunction<String> types = getTypeFunction();
        Form.Policy policy = new Form.Policy();
        policy.name = titel;
        policy.balances = new ArrayList<>();
        for (int i=1; i<=values.length; i++) {
            policy.balances.add(new Form.Balance(types.apply(i), values[i-1]));
        }
        return policy;
    }
}
