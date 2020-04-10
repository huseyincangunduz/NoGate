package com.esenlermotionstar.nogate.Helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexProcessing {

    public static Matcher getMatcher(String regularExpressionPattern, String findingStr) {
        //String pattern = "^http(s*):\\/\\/(m|(\\w*www\\w*))\\.youtube\\.com";//"(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(regularExpressionPattern);
        Matcher matcher = compiledPattern.matcher(findingStr);
        return matcher;
    }

    public static Boolean contains(Matcher regexMatcher) {
        return regexMatcher.find();
    }

    public static Boolean contains(String regularExpressionPattern, String findingStr) {
        return contains(getMatcher(regularExpressionPattern, findingStr));
    }

    public static String getGroup(Matcher regexMatcher) {
        if (contains(regexMatcher)) {
            return regexMatcher.group();
        } else return null;
    }

    public static String getFirstGroup(String regularExpressionPattern, String findingStr)
    {
        return getGroup(getMatcher(regularExpressionPattern,findingStr));
    }


    public static String getYouTubeVideoID(String url)
    {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        return getFirstGroup(pattern,url);
    }

}
