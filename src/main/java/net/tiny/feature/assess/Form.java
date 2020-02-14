package net.tiny.feature.assess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Form : 所有项目集合评价策略表。
 * JSON格式大小约8K
 *
 */
public final class Form {
    public static final String VERSION = Version.CURRENT.ver();

    //一个项目的在指定评价分类的配比(加权)
    public static class Balance {
        public String type = null;
        public double weight = 0.0d;

        public Balance() {}
        public Balance(String t, double w) {
            if (w > 0.99d) {
                throw new IllegalArgumentException(String.format("'%s' balance weight '%.2f' must be less than 0.99", t, w));
            }
            if (w < 0.0d) {
                weight = 0.0d;
            }
            weight = w;
            type = t;
        }
        public boolean isValid() {
            return (null != type && weight > 0.0d);
        }
    }

    //一个项目所有评价加权的策略
    public static class Policy {
        public String name;
        public List<Balance> balances;

        public String getName() {
            return name;
        }
        public boolean hasBalance(final String type) {
            return balances.stream()
                .anyMatch(b -> b.isValid() && b.type.equals(type));
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Policy))
                return false;
            return name.equalsIgnoreCase(((Policy)o).name);
        }
    }

    public static class Table {
        public String title;
        public double[] weights;
    }

    /**
     * 用于评估计算的评价分类表。
     * JSON格式大小约1K
     */
    public static class Tables {
        public String ver = VERSION;
        public String id;
        public List<String> types;
        public List<Table> tables;
        public long modified = System.currentTimeMillis();
        public long expires = System.currentTimeMillis() + (20L*24L*3600L); // 20Days
        public String signature;

        public Double[][] values() {
            if (null == tables || tables.isEmpty()) {
                return null;
            }
            int size = tables.get(0).weights.length;
            Double[][] values = new Double[tables.size()][size];
            for (int i=0; i<tables.size(); i++) {
                for (int j=0; j<size; j++) {
                    values[i][j] = tables.get(i).weights[j];
                }
            }
            return values;
        }

        public Matrix<Double> matrix() {
            return Matrix.of(values());
        }
    }

    public String ver = VERSION;
    public String id;
    public List<Policy> policies;
    public long modified;
    public long expires; // 20Days
    public String signature;

    /**
     * 调整评价策略表
     * @param revision
     * @return
     */
    public Form apply(Form revision) {
        if(null == policies || revision == null) {
            return this;
        }
        if (!ver.equals(revision.ver)) {
            throw new IllegalArgumentException(String.format("Inconsistent version('%s' - '%s)", ver, revision.ver));
        }
        for (Policy policy : revision.policies) {
            Optional<Policy> target = getPolicy(policy.name);
            if (target.isPresent()) {
                // Apply a exist policy
                apply(policy);
            } else {
                //Append a new policy
                policies.add(policy);
            }
        }
        return this;
    }

    private void apply(Policy revision) {
        final List<Balance> revs = revision.balances;
        Policy policy = getPolicy(revision.name).get();
        for (Balance rev : revs) {
            Optional<Balance> b = getBalance(revision.name, rev.type);
            if (!b.isPresent()) {
                //Append a new balance
                policy.balances.add(rev);
            } else {
                //Replace a exist balance weight
                b.get().weight = rev.weight;
            }
        }
    }

    public Form appendPolicy(Policy policy) {
        if (null == policies) {
            policies = new ArrayList<>();
        }
        if (!policies.contains(policy))
            policies.add(policy);
        return this;
    }

    public Optional<Policy> getPolicy(String name) {
        if(null == policies) {
            return Optional.empty();
        }
        return policies.stream()
                .filter(p -> p.name.equals(name))
                .findFirst();
    }

    public List<String> getTypes() {
        List<String> types = new ArrayList<>();
        //设置评价分类名
        for (int i=0; i<policies.size(); i++) {
            Policy policy = policies.get(i);
            for (int n=0; n<policy.balances.size(); n++) {
                Balance balance = policy.balances.get(n);
                if (!types.contains(balance.type)) {
                    types.add(balance.type);
                }
            }
        }
        return Collections.unmodifiableList(types);
    }

    public String[] getTitles() {
        //设置评价分类名
        return policies.stream()
                .map(Policy::getName)
                .toArray(n -> new String[n]);
    }

    public Optional<Balance> getBalance(String name, String type) {
        Optional<Policy> policy = getPolicy(name);
        if (policy.isPresent()) {
            return policy.get()
                    .balances
                    .stream()
                    .filter(b -> b.type.equals(type))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public double getWeight(String policy, String type) {
        Optional<Balance> balance = getBalance(policy, type);
        if (balance.isPresent()) {
            return balance.get().weight;
        } else {
            return 0.0d;
        }
    }

    public double[] getOrderedWeights(String name, List<String> types) {
        final double[] weights = new double[types.size()];
        Arrays.fill(weights, 0.0d);
        final Optional<Policy> policy = getPolicy(name);
        if (!policy.isPresent()) {
            return weights;
        }
        final List<Balance> list = policy.get().balances;
        for (int i=0; i<types.size(); i++) {
            final String type = types.get(i);
            Optional<Balance> balance = list.stream()
                    .filter(b -> b.type.equals(type))
                    .findFirst();
            if (balance.isPresent()) {
                weights[i] = balance.get().weight;
            }
        }
        return weights;
    }

    public boolean isExpired() {
        return expires > System.currentTimeMillis();
    }

    public boolean validate() {
        if(null == policies) {
            return false;
        }
        //TODO
        return true;
    }

    /**
     * 返回指定分类的所有加权配比
     * @param type
     * @return
     */
    public Map<String, Double> weights(String type) {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (Policy policy : policies) {
            double w = policy.balances
                        .stream()
                        .filter(b -> b.type.equals(type))
                        .findFirst()
                        .get()
                        .weight;
            weights.put(policy.name, w);
        }
        return  weights;
    }

    public Tables toTables() {
        return toTables(getTitles());
    }

    public Tables toTables(String[] titles) {
        Tables tables = new Tables();
        tables.id = id;
        tables.expires = expires;
        tables.signature = signature;
        //设置评价分类名
        tables.types = getTypes();
        tables.tables = new ArrayList<>();

        //设置评价分类的配比(加权)
        for (int i=0; i<titles.length; i++) {
            Optional<Policy> opt = getPolicy(titles[i]);
            if (!opt.isPresent())
                continue; //Not found skip
            Table tab = new Table();
            tab.title = titles[i];
            tab.weights = getOrderedWeights(tab.title, tables.types);
            tables.tables.add(tab);
        }
        Collections.unmodifiableList(tables.tables);
        return tables;
    }
}
