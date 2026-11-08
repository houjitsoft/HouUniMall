package com.houjiang.base.support;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件识别规则引擎（Dify 不可用时的本地兜底）
 * @author Amos
 */
public final class FileRecognizeRuleHelper {

    private FileRecognizeRuleHelper() {
    }

    public static Map<String, Object> recognize(String fileName, byte[] fileBytes, String description) {
        String recognizeType = recognizeFileType(fileName, fileBytes);
        String summary = extractSummary(fileName, fileBytes, recognizeType, description);
        Map<String, Object> result = new HashMap<>();
        result.put("type", recognizeType);
        result.put("summary", summary);
        result.put("fileName", fileName);
        result.put("fileSize", fileBytes != null ? fileBytes.length : 0);
        result.put("source", "rule");
        return result;
    }

    private static String recognizeFileType(String fileName, byte[] fileBytes) {
        if (fileName == null) {
            return "unknown";
        }
        if (isContractFile(fileName, fileBytes)) {
            return "contract";
        }
        if (isReceivableFile(fileName, fileBytes)) {
            return "receivable";
        }
        if (isPayableFile(fileName, fileBytes)) {
            return "payable";
        }
        if (isGoodsFile(fileName, fileBytes)) {
            return "goods";
        }
        if (isDocumentFile(fileName, fileBytes)) {
            return "document";
        }
        if (isInvoiceFile(fileName, fileBytes)) {
            return "invoice";
        }
        return "unknown";
    }

    private static boolean isContractFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("合同") || lowerName.contains("contract")
            || lowerName.contains("协议") || lowerName.contains("agreement")) {
            return true;
        }
        return containsText(fileBytes, "合同", "甲方", "乙方", "签订日期");
    }

    private static boolean isReceivableFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("应收") || lowerName.contains("receivable")
            || lowerName.contains("收款") || lowerName.contains("账单")) {
            return true;
        }
        return containsText(fileBytes, "应收", "应收账款", "收款单", "账单");
    }

    private static boolean isPayableFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("应付") || lowerName.contains("payable")
            || lowerName.contains("付款") || lowerName.contains("发票")) {
            return true;
        }
        return containsText(fileBytes, "应付", "应付账款", "付款单", "采购发票");
    }

    private static boolean isGoodsFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("商品") || lowerName.contains("goods")
            || lowerName.contains("产品") || lowerName.contains("product")
            || lowerName.contains("清单") || lowerName.contains("catalog")) {
            return true;
        }
        return containsText(fileBytes, "商品", "产品", "规格", "价格表");
    }

    private static boolean isDocumentFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("单据") || lowerName.contains("凭证")
            || lowerName.contains("票据") || lowerName.contains("单证")) {
            return true;
        }
        return containsText(fileBytes, "单据", "凭证", "票据编号");
    }

    private static boolean isInvoiceFile(String fileName, byte[] fileBytes) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.contains("发票") || lowerName.contains("invoice")
            || lowerName.contains("fapiao") || lowerName.contains("dzfp")) {
            return true;
        }
        return containsText(fileBytes, "发票", "发票代码", "发票号码", "税额");
    }

    private static boolean containsText(byte[] fileBytes, String... keywords) {
        if (fileBytes == null || fileBytes.length == 0 || fileBytes.length > 500_000) {
            return false;
        }
        try {
            String content = new String(fileBytes, "UTF-8");
            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return false;
    }

    private static String extractSummary(String fileName, byte[] fileBytes, String recognizeType, String description) {
        StringBuilder summary = new StringBuilder();
        // 文件识别接口传入的是 OCR/JSON 指令，不是业务说明，不能原样写入识别摘要。
        if (isUserDescription(description)) {
            summary.append("说明: ").append(description.trim()).append("; ");
        }
        try {
            String title = extractTitle(fileName, fileBytes);
            if (title != null && !title.isEmpty()) {
                summary.append("标题: ").append(title).append("; ");
            }
            if (fileName != null) {
                // 仅接受合理长度的金额，避免把上传文件常见的 13 位时间戳/流水号误当成金额。
                // 带“元/万”或带小数的金额均可识别；纯整数金额最多 7 位。
                Pattern amountPattern = Pattern.compile("(?<!\\d)(\\d{1,7}(?:[,.]\\d{1,2})?)(?:\\s*[万元]|\\s*元)?(?!\\d)");
                Matcher matcher = amountPattern.matcher(fileName);
                if (matcher.find()) {
                    summary.append("金额: ").append(matcher.group(1)).append("; ");
                }
            }
            if (fileBytes != null && fileBytes.length < 100_000) {
                String content = new String(fileBytes, "UTF-8");
                Pattern customerPattern = Pattern.compile("(?:客户|甲方|委托方|发包方)[：:]([^\\n\\r]{2,30})");
                Matcher customerMatcher = customerPattern.matcher(content);
                if (customerMatcher.find()) {
                    String customer = customerMatcher.group(1).trim().replaceAll("[：:()（）\\s]+", "").trim();
                    if (customer.length() > 1 && customer.length() <= 30) {
                        summary.append("客户: ").append(customer).append("; ");
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        if (summary.length() == 0) {
            summary.append("已识别为").append(getTypeName(recognizeType));
        }
        return summary.toString();
    }

    private static boolean isUserDescription(String description) {
        if (description == null || description.isBlank()) return false;
        String text = description.trim().toLowerCase();
        return text.length() <= 80
                && !text.contains("ocr")
                && !text.contains("json")
                && !text.contains("识别附件")
                && !text.contains("提取金额");
    }

    private static String extractTitle(String fileName, byte[] fileBytes) {
        if (fileName != null) {
            String nameWithoutExt = fileName;
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                nameWithoutExt = fileName.substring(0, lastDot);
            }
            nameWithoutExt = nameWithoutExt
                .replaceAll("\\d+[万元]", "")
                .replaceAll("[¥￥]\\d+", "")
                .replaceAll("\\d{4}[-年]\\d{1,2}[-月]\\d{1,2}[日]?", "")
                .replaceAll("费用|金额|价格|总价", "")
                .trim();
            if (nameWithoutExt.length() > 2) {
                // 纯数字文件名通常是对象存储时间戳或流水号，不应作为业务标题回填。
                if (nameWithoutExt.matches("[\\d_\\- ]{8,}")) {
                    return null;
                }
                return nameWithoutExt;
            }
        }
        return null;
    }

    private static String getTypeName(String type) {
        return switch (type) {
            case "contract" -> "合同";
            case "receivable" -> "应收";
            case "payable" -> "应付";
            case "goods" -> "商品";
            case "document" -> "单据";
            case "invoice" -> "发票";
            default -> "未知类型";
        };
    }

    public static String getRedirectUrl(String recognizeType) {
        Map<String, String> urlMap = Map.of(
            "contract", "/oa/contract/list",
            "receivable", "/finance/fund/receivable",
            "payable", "/finance/fund/payable",
            "goods", "/goods/list",
            "document", "/oa/document/list",
            "invoice", "/finance/invoice/list"
        );
        return urlMap.getOrDefault(recognizeType, "");
    }
}
