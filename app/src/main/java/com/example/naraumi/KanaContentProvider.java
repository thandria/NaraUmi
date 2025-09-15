package com.example.naraumi;

public class KanaContentProvider {

        public static String[][] getKanaGroup(String lessonType, String lessonGroup) {
        
        if ("HIRAGANA".equals(lessonType)) {
            return getHiraganaContent(lessonGroup);
        } 
        else if ("KATAKANA".equals(lessonType)) {
            return getKatakanaContent(lessonGroup);
        } 
        else if ("BASICS 1".equals(lessonType)) {
            return getBasicsContent(lessonGroup);
        } 
        else if ("BASICS 2".equals(lessonType)) {
            return getBasics2Content(lessonGroup);
        } 
        else if ("ADVANCED 1".equals(lessonType)) {
            return getAdvanced1Content(lessonGroup);
        } 
        else if ("ADVANCED 2".equals(lessonType)) {
            return getAdvanced2Content(lessonGroup);
        }

                return new String[][]{};
    }

        private static String[][] getHiraganaContent(String group) {
        switch (group) {
            case "あーか":  // A-Ka group: vowels and K-sounds
                return new String[][]{
                    {"あ", "a", "a"}, {"い", "i", "i"}, {"う", "u", "u"}, {"え", "e", "e"}, {"お", "o", "o"},
                    {"か", "ka", "ka"}, {"き", "ki", "ki"}, {"く", "ku", "ku"}, {"け", "ke", "ke"}, {"こ", "ko", "ko"}
                };
                
            case "さーた":  // Sa-Ta group: S-sounds and T-sounds
                return new String[][]{
                    {"さ", "sa", "sa"}, {"し", "shi", "shi"}, {"す", "su", "su"}, {"せ", "se", "se"}, {"そ", "so", "so"},
                    {"た", "ta", "ta"}, {"ち", "chi", "chi"}, {"つ", "tsu", "tsu"}, {"て", "te", "te"}, {"と", "to", "to"}
                };
                
            case "なーは":  // Na-Ha group: N-sounds and H-sounds
                return new String[][]{
                    {"な", "na", "na"}, {"に", "ni", "ni"}, {"ぬ", "nu", "nu"}, {"ね", "ne", "ne"}, {"の", "no", "no"},
                    {"は", "ha", "ha"}, {"ひ", "hi", "hi"}, {"ふ", "fu", "fu"}, {"へ", "he", "he"}, {"ほ", "ho", "ho"}
                };
                
            case "ま":      // Ma group: M-sounds
                return new String[][]{
                    {"ま", "ma", "ma"}, {"み", "mi", "mi"}, {"む", "mu", "mu"}, {"め", "me", "me"}, {"も", "mo", "mo"}
                };
                
            case "やーら":  // Ya-Ra group: Y-sounds and R-sounds
                return new String[][]{
                    {"や", "ya", "ya"}, {"ゆ", "yu", "yu"}, {"よ", "yo", "yo"},
                    {"ら", "ra", "ra"}, {"り", "ri", "ri"}, {"る", "ru", "ru"}, {"れ", "re", "re"}, {"ろ", "ro", "ro"}
                };
                
            case "わーんーを": // Wa-N-Wo group: remaining sounds
                return new String[][]{
                    {"わ", "wa", "wa"}, {"を", "wo", "wo"}, {"ん", "n", "n"}
                };
        }
        return new String[][]{};
    }

        private static String[][] getKatakanaContent(String group) {
        switch (group) {
            case "アーカ":  // A-Ka group: vowels and K-sounds
                return new String[][]{
                    {"ア", "a", "a"}, {"イ", "i", "i"}, {"ウ", "u", "u"}, {"エ", "e", "e"}, {"オ", "o", "o"},
                    {"カ", "ka", "ka"}, {"キ", "ki", "ki"}, {"ク", "ku", "ku"}, {"ケ", "ke", "ke"}, {"コ", "ko", "ko"}
                };
                
            case "サーター": // Sa-Ta group: S-sounds and T-sounds
                return new String[][]{
                    {"サ", "sa", "sa"}, {"シ", "shi", "shi"}, {"ス", "su", "su"}, {"セ", "se", "se"}, {"ソ", "so", "so"},
                    {"タ", "ta", "ta"}, {"チ", "chi", "chi"}, {"ツ", "tsu", "tsu"}, {"テ", "te", "te"}, {"ト", "to", "to"}
                };
                
            case "ナーハ":  // Na-Ha group: N-sounds and H-sounds
                return new String[][]{
                    {"ナ", "na", "na"}, {"ニ", "ni", "ni"}, {"ヌ", "nu", "nu"}, {"ネ", "ne", "ne"}, {"ノ", "no", "no"},
                    {"ハ", "ha", "ha"}, {"ヒ", "hi", "hi"}, {"フ", "fu", "fu"}, {"ヘ", "he", "he"}, {"ホ", "ho", "ho"}
                };
                
            case "マ":      // Ma group: M-sounds
                return new String[][]{
                    {"マ", "ma", "ma"}, {"ミ", "mi", "mi"}, {"ム", "mu", "mu"}, {"メ", "me", "me"}, {"モ", "mo", "mo"}
                };
                
            case "ヤーラ":  // Ya-Ra group: Y-sounds and R-sounds
                return new String[][]{
                    {"ヤ", "ya", "ya"}, {"ユ", "yu", "yu"}, {"ヨ", "yo", "yo"},
                    {"ラ", "ra", "ra"}, {"リ", "ri", "ri"}, {"ル", "ru", "ru"}, {"レ", "re", "re"}, {"ロ", "ro", "ro"}
                };
                
            case "ワーンーヲ": // Wa-N-Wo group: remaining sounds
                return new String[][]{
                    {"ワ", "wa", "wa"}, {"ヲ", "wo", "wo"}, {"ン", "n", "n"}
                };
        }
        return new String[][]{};
    }

        private static String[][] getBasicsContent(String group) {
        switch (group) {
            case "Greetings 1":                  return new String[][]{
                    {"こんにちは", "Hello", "konnichiwa"},
                    {"お元気ですか", "How are you?", "ogenki desu ka"},
                    {"元気です", "I'm fine", "genki desu"}
                };
                
            case "Greetings 2":                  return new String[][]{
                    {"ありがとう", "Thank you", "arigatou"},
                    {"はじめまして", "Nice to meet you", "hajimemashite"},
                    {"じゃあね", "See you later", "jaa ne"}
                };
            
            case "Greetings 3":                 return new String[][]{
                    {"おはようございます", "Good morning", "ohayou gozaimasu"},
                    {"こんばんは", "Good evening", "konbanwa"},
                    {"おやすみなさい", "Good night", "oyasuminasai"}
                };
                
            case "Office Items":                  return new String[][]{
                    {"マウス", "Mouse", "mausu"},
                    {"キーボード", "Keyboard", "kiiboodo"},
                    {"テレビ", "TV", "terebi"},
                    {"本", "Book", "hon"}
                };

            case "Survival Phrases":                 return new String[][]{
                    {"トイレはどこですか", "Where is the toilet?", "toire wa doko desu ka"},
                    {"これはいくらですか", "How much is this?", "kore wa ikura desu ka"},
                    {"わかりません", "I don't understand", "wakarimasen"}
                };
        }
        return new String[][]{};
    }

        private static String[][] getBasics2Content(String group) {
        switch (group) {
            case "Classroom 1":                  return new String[][]{
                    {"教室", "Classroom", "kyoushitsu"},
                    {"先生", "Teacher", "sensei"},
                    {"学生", "Student", "gakusei"},
                    {"宿題", "Homework", "shukudai"}
                };
                
            case "Classroom 2":                  return new String[][]{
                    {"試験", "Exam", "shiken"},
                    {"授業", "Class/Lesson", "jugyou"},
                    {"質問", "Question", "shitsumon"},
                    {"答え", "Answer", "kotae"}
                };
                
            case "Class Items":                  return new String[][]{
                    {"ペン", "Pen", "pen"},
                    {"鉛筆", "Pencil", "enpitsu"},
                    {"ノート", "Notebook", "nooto"},
                    {"消しゴム", "Eraser", "keshigomu"},
                    {"定規", "Ruler", "jougi"}
                };
        }
        return new String[][]{};
    }

        private static String[][] getAdvanced1Content(String group) {
        switch (group) {
            case "Travel 1":                  return new String[][]{
                    {"空港", "Airport", "kuukou"},
                    {"駅", "Station", "eki"},
                    {"ホテル", "Hotel", "hoteru"},
                    {"切符", "Ticket", "kippu"},
                    {"荷物", "Luggage", "nimotsu"}
                };
                
            case "Travel 2":                  return new String[][]{
                    {"地図", "Map", "chizu"},
                    {"道", "Road/Way", "michi"},
                    {"方向", "Direction", "houkou"},
                    {"右", "Right", "migi"},
                    {"左", "Left", "hidari"}
                };
                
            case "Food Items":                  return new String[][]{
                    {"寿司", "Sushi", "sushi"},
                    {"ラーメン", "Ramen", "raamen"},
                    {"お米", "Rice", "okome"},
                    {"野菜", "Vegetables", "yasai"},
                    {"肉", "Meat", "niku"}
                };
                
            case "Restaurant":                  return new String[][]{
                    {"メニュー", "Menu", "menyuu"},
                    {"注文", "Order", "chuumon"},
                    {"お会計", "Bill/Check", "okaikei"},
                    {"美味しい", "Delicious", "oishii"},
                    {"飲み物", "Drinks", "nomimono"}
                };
        }
        return new String[][]{};
    }

        private static String[][] getAdvanced2Content(String group) {
        switch (group) {
            case "Business 1":                  return new String[][]{
                    {"会社", "Company", "kaisha"},
                    {"会議", "Meeting", "kaigi"},
                    {"仕事", "Work/Job", "shigoto"},
                    {"プロジェクト", "Project", "purojekuto"},
                    {"報告", "Report", "houkoku"}
                };
                
            case "Business 2":                  return new String[][]{
                    {"契約", "Contract", "keiyaku"},
                    {"交渉", "Negotiation", "koushou"},
                    {"提案", "Proposal", "teian"},
                    {"締切", "Deadline", "shimekiri"},
                    {"成功", "Success", "seikou"}
                };
                
            case "Formal Greetings":                  return new String[][]{
                    {"お疲れ様です", "Thank you for your hard work", "otsukaresama desu"},
                    {"申し訳ございません", "I sincerely apologize", "moushiwake gozaimasen"},
                    {"恐れ入ります", "Excuse me (formal)", "osore irimasu"},
                    {"失礼いたします", "Excuse me (leaving)", "shitsurei itashimasu"},
                    {"よろしくお願いします", "Please treat me favorably", "yoroshiku onegaishimasu"}
                };
                
            case "Polite Speech":                  return new String[][]{
                    {"いらっしゃいませ", "Welcome (to customer)", "irasshaimase"},
                    {"恐縮です", "I'm sorry/grateful", "kyoushuku desu"},
                    {"お忙しい", "Busy (respectful)", "oisogashii"},
                    {"ご確認", "Confirmation (respectful)", "gokakunin"},
                    {"お手伝い", "Assistance (respectful)", "otetsudai"}
                };
        }
        return new String[][]{};
    }
}