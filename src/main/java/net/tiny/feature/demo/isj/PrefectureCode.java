package net.tiny.feature.demo.isj;

import java.util.HashMap;
import java.util.Map;

public enum PrefectureCode {
    北海("01", "北海道"),
    青森("02", "青森県"),
    岩手("03", "岩手県"),
    宮城("04", "宮城県"),
    秋田("05", "秋田県"),
    山形("06", "山形県 "),
    福島("07", "福島県"),
    茨城("08", "茨城県"),
    栃木("09", "栃木県"),
    群馬("10", "群馬県"),
    埼玉("11", "埼玉県"),
    千葉("12", "千葉県"),
    東京("13", "東京都"),
    神奈川("14", "神奈川県"),
    新潟("15", "新潟県"),
    富山("16", "富山県"),
    石川("17", "石川県"),
    福井("18", "福井県 "),
    山梨("19", "山梨県"),
    長野("20", "長野県"),
    岐阜("21", "岐阜県"),
    静岡("22", "静岡県"),
    愛知("23", "愛知県"),
    三重("24", "三重県"),
    滋賀("25", "滋賀県"),
    京都("26", "京都府"),
    大阪("27", "大阪府 "),
    兵庫("28", "兵庫県"),
    奈良("29", "奈良県"),
    和歌山("30", "和歌山県"),
    鳥取("31", "鳥取県"),
    島根("32", "島根県"),
    岡山("33", "岡山県"),
    広島("34", "広島県"),
    山口("35", "山口県"),
    徳島("36", "徳島県"),
    香川("37", "香川県"),
    愛媛("38", "愛媛県"),
    高知("39", "高知県"),
    福岡("40", "福岡県"),
    佐賀("41", "佐賀県"),
    長崎("42", "長崎県"),
    熊本("43", "熊本県"),
    大分("44", "大分県 "),
    宮崎("45", "宮崎県"),
    鹿児島("46", "鹿児島県 "),
    沖縄("47", "沖縄県");

    final String code, name;
    PrefectureCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String code() {
        return code;
    }

    public String localName() {
        return name;
    }

    private static Map<String, PrefectureCode> map = new HashMap<>();
    static {
        for(PrefectureCode pc : PrefectureCode.values()){
            map.put(pc.code, pc);
        }
    }

    public static PrefectureCode valueOf(int code) {
        return map.get(String.format("%02d", code));
    }
}
