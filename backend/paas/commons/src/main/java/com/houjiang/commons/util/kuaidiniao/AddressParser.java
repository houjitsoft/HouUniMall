package com.houjiang.commons.util.kuaidiniao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 地址解析工具类，用于将完整的地址字符串解析成省份、城市、区县和详细地址
 * @author Amos
 */
public class AddressParser {

    public static class AddressResult {
        private String province;
        private String city;
        private String district;
        private String detailAddress;

        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    }

    public static AddressResult parseAddress(String address) {
        AddressResult result = new AddressResult();
        String remaining = address
                .replaceAll("^(中国-|-)\\s*-?\\s*", "")
                .replaceAll("\\s+", "")
                .trim();

        result.setProvince(extractProvince(remaining));
        remaining = remaining.substring(result.getProvince().length()).trim();

        if (isMunicipality(result.getProvince())) {
            result.setCity(result.getProvince() + "市");
        } else {
            result.setCity(extractCity(remaining));
            remaining = remaining.substring(result.getCity().length()).trim();
        }
        remaining = remaining.replaceFirst("^市", "").trim();

        result.setDistrict(extractDistrict(remaining));
        remaining = remaining.substring(result.getDistrict().length()).trim();

        result.setDetailAddress(remaining);

        return result;
    }

    private static String extractProvince(String address) {
        Pattern pattern = Pattern.compile("^(北京|上海|天津|重庆|.+?(?:自治区|省))");
        Matcher matcher = pattern.matcher(address);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("省份解析失败: " + address);
    }

    private static boolean isMunicipality(String province) {
        return province.matches("^北京|上海|天津|重庆$");
    }

    private static String extractCity(String remaining) {
        Pattern pattern = Pattern.compile("^(.*?自治州|.*?市)");
        Matcher matcher = pattern.matcher(remaining);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("城市解析失败: " + remaining);
    }

    /**
     * 修改点：区县正则增加"市"的匹配
     */
    private static String extractDistrict(String remaining) {
        // 允许匹配以市结尾的区县
        Pattern pattern = Pattern.compile("^(.*?[区县市])");
        Matcher matcher = pattern.matcher(remaining);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("区县解析失败: " + remaining);
    }
}