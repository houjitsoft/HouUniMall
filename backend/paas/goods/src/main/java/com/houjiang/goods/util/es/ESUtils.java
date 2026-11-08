package com.houjiang.goods.util.es;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.ModelLoader;
import ai.djl.translate.Transform;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.commons.dto.shop.ArticleDTO;
import com.houjiang.commons.dto.shop.ArticleShopDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import com.houjiang.commons.vo.shop.ArticleVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.goods.dto.es.ESCodeEnume;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.GoodsCommonAttributeAssociationMapper;
import com.houjiang.goods.service.ElasticSearchService;
import jakarta.annotation.PostConstruct;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * es工具类
 * @author Amos
 */
@Component
@RefreshScope
public class ESUtils {
    public static final Logger logger = LoggerFactory.getLogger(ESUtils.class);
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static RestHighLevelClient client;
    /** 批量操作的对象 */
    private static final BulkProcessor bulkProcessor;
    @Autowired
    private GoodsCommonAttributeAssociationMapper goodsCommonAttributeAssociationMapper;

    private  static GoodsCommonAttributeAssociationMapper esGoodsCommonAttributeAssociationMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    private static BaseServiceClient esBaseServiceClient;

    /**
     * @PostContruct是spring框架的注解 spring容器初始化的时候执行该方法
     */
    @PostConstruct
    public void init() {
        client = this.restHighLevelClient;
        this.esGoodsCommonAttributeAssociationMapper = this.goodsCommonAttributeAssociationMapper;
        this.esBaseServiceClient = this.baseServiceClient;

    }
    static {
        bulkProcessor = createBulkProcessor();
    }


    @Value("${houcloud.goods.es.index}")
    private  String esindex ;
    @Value("${houcloud.goods.es.index2}")
    private  String esindex2 ;
    @Value("${houcloud.goods.es.sensitive}")
    private  String esSensitive ;
    @Value("${houcloud.goods.es.type:article}")
    private  String estype ;


    private static String index;
    private static String index2;
    private static String sensitiveIndex;
    private static String type;

    @PostConstruct
    public String getIndex() {
        return index = this.esindex;
    }
    @PostConstruct
    public String getIndex2() {
        return index2 = this.esindex2 ;
    }
    @PostConstruct
    public String getSensitiveIndex() {
        return sensitiveIndex = this.esSensitive ;
    }
    @PostConstruct
    public String getType() {
        return type = this.estype;
    }

    private static ElasticSearchService elasticSearchService;

    @Autowired
    public void setElasticSearchService(ElasticSearchService elasticSearchService) {
        ESUtils.elasticSearchService = elasticSearchService;
    }
    /** 定义图像大小的常量，用于指定模型输入图像的尺寸 */
    private static final int IMAGE_SIZE = 224;

    /** 模型类的实例，用于加载和管理机器学习模型 */
    private static Model model;

    /** 预测器实例，用于对输入图像进行预测，predictor.predict(input)相当于Python中的model(input) */
    private static Predictor<Image, float[]> predictor;

    /** 模型文件名常量，指定模型文件的名称 */
    private static final String MODEL_FILE_NAME = "model.pt";

    /** 模型URL常量，用于从指定的URL下载模型文件 */
    private static final String MODEL_URL = "https://b2c-test.unimall.cn/modelPt/model.pt";

    // 静态代码块用于初始化模型和预测器
    static {
        try {
            // 获取当前工作目录
            Path currentDir = Paths.get("").toAbsolutePath();
            // 构建模型文件的路径
            Path modelifPath = currentDir.resolve(MODEL_FILE_NAME);
            // 如果模型文件不存在，则下载模型
            if (!Files.exists(modelifPath)) {
                downloadModel(modelifPath);
            }
            // 初始化模型实例
            model = Model.newInstance("model");
            // 加载模型
            model.load(new FileInputStream(modelifPath.toFile()));
            // 定义图像预处理步骤
            Transform resize = new Resize(IMAGE_SIZE);
            Transform toTensor = new ToTensor();
            Transform normalize = new Normalize(new float[]{0.485f, 0.456f, 0.406f}, new float[]{0.229f, 0.224f, 0.225f});
            // 定义Translator接口的匿名实现类，用于处理输入图像和输出结果
            Translator<Image, float[]> translator = new Translator<Image, float[]>() {
                @Override
                public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
                    NDManager ndManager = ctx.getNDManager();
                    // 打印输入图像的尺寸
                    System.out.println("input: " + input.getWidth() + ", " + input.getHeight());
                    // 对输入图像进行预处理
                    NDArray transform = normalize.transform(toTensor.transform(resize.transform(input.toNDArray(ndManager))));
                    // 打印预处理后图像的形状
                    System.out.println(transform.getShape());
                    NDList list = new NDList();
                    list.add(transform);
                    return list;
                }
                @Override
                public float[] processOutput(TranslatorContext ctx, NDList ndList) throws Exception {
                    // 将模型输出转换为float数组
                    return ndList.get(0).toFloatArray();
                }
            };
            // 初始化预测器
            predictor = new Predictor<>(model, translator, Device.cpu(), true);
        } catch (Exception e) {
            // 打印异常信息但不影响应用启动
            logger.error("PyTorch 模型初始化失败，将禁用图像处理功能: {}", e.getMessage());
        }
    }

    /**
     * 创建商品索引库
     * @throws Exception
     */
    public static ResponseResult setMappings(){
        try {
            //使用client对象创建一个索引库
            //参数1：创建索引请求对象 参数2：请求配置对象
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);

            //创建一个Mappings信息
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    //图片向量
                    .startObject("vector")
                    .field("type", "dense_vector")
                    .field("dims", 1024)
                    .endObject()
                    //商品id
                    .startObject("commonId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //货品id
                    .startObject("goodsId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //商品名称
                    .startObject("commonName")
                    .field("type", "text")
                    .field("store", true)
                    .startObject("fields")
                    //精准匹配字段
                    .startObject("raw")
                    .field("type", "keyword")
                    .endObject()
                    //分词匹配字段
                    .startObject("chinese")
                    .field("type", "text")
//                    .field("analyzer", "ik_max_word")
//                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .endObject()
                    .endObject()

                    // 1普通商品  2供应商商品 3品牌商品
                    .startObject("goodsType")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()

                    //品牌名称
                    .startObject("brandName")
                    .field("type", "text")
                    .field("store", true)
                    .field("analyzer", "ik_max_word")
                    .endObject()
                    //品牌id
                    .startObject("brandId")
                    .field("type", "text")
                    .field("store", true)
                    .field("analyzer", "ik_max_word")
                    .endObject()

                    //商品属性内容
                    .startObject("attributeContentId")
                    .field("type", "text")
                    .field("store", true)
                    .field("analyzer", "ik_max_word")
                    .endObject()

                    //商品分类id
                    .startObject("catId")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()

                    //商品价格
                    .startObject("commonPrice")
                    .field("type", "double")
                    .field("store", true)
                    .endObject()
                    //商品评论数
                    .startObject("commonEvaluate")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //商品收藏量
                    .startObject("commonCollect")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //商品销量
                    .startObject("commonSalenum")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //是否自营商品 1 不是 2 是
                    .startObject("shopSelfSupport")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //店铺id
                    .startObject("shopId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //店铺名
                    .startObject("shopName")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()
                    //商品类型
                    .startObject("commonType")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //等级id
                    .startObject("levelId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //过期退款 0 是 1 否
                    .startObject("pastDue")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //活动
                    .startObject("commonIsXian")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    // 商品列表优先展示
                    .startObject("goodsListFirstShow")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //虚拟商品 0 是 1 否
                    .startObject("virtualGoods")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //商品图片
                    .startObject("commonImage")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()
                    //副标题，即商品广告词
                    .startObject("commonPromotionTips")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()
                    //平均发货时间
                    .startObject("avgShippingTime")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()
                    //虚拟商品有效期
                    .startObject("periodOfValidity")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()
                    //小宝价格
                    .startObject("commonBabyPrice")
                    .field("type", "double")
                    .field("store", true)
                    .endObject()
                    //市场价
                    .startObject("commonMarketPrice")
                    .field("type", "double")
                    .field("store", true)
                    .endObject()
                    //更新时间
                    .startObject("updateTime")
                    .field("type", "date")
                    .field("store", true)
                    .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                    .endObject()
                    .endObject()
                    .endObject();
            createIndexRequest.mapping(builder);
            //使用client把mapping信息设置到索引库中
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            return ResponseResult.success(ESCodeEnume.INDEX_CREATE_SUCCESS.getCode(), ESCodeEnume.INDEX_CREATE_SUCCESS.getMsg());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INDEX_CREATE_FAIL.getCode(), ESCodeEnume.INDEX_CREATE_FAIL.getMsg());
    }
    public static ResponseResult deleteGoodsIndex() {
        boolean acknowledged = false;
        try {
            //创建删除索引请求
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
            //        执行
            AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //得到相应
            acknowledged = delete.isAcknowledged();
            if(acknowledged){
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_SUCCESS.getCode(), ESCodeEnume.INDEX_DELETE_SUCCESS.getMsg());
            }else{
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_FAIL.getCode(), ESCodeEnume.INDEX_DELETE_FAIL.getMsg());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INDEX_DELETE_FAIL.getCode(), ESCodeEnume.INDEX_DELETE_FAIL.getMsg());
    }
    /** 批量导入商品 */
    public static ResponseResult importData(List<ArticleDTO> articleDTOS){
        try {
            //先判断索引库是否存在 不存在 就创建
            GetIndexRequest request = new GetIndexRequest();
            boolean exists = client.indices().exists(request.indices(index), RequestOptions.DEFAULT);
            if(exists) {
                System.out.println("商品索引库存在");
            }else{
                System.out.println("商品索引库不存在");
                ResponseResult resp = setMappings();
                System.out.println(resp.getMessage());
            }
            List<Integer> commonIds = new ArrayList<>();
            List<IndexRequest> indexRequests = new ArrayList<>();
            if(null != articleDTOS && !articleDTOS.isEmpty()){
                articleDTOS.forEach(e -> {
                    IndexRequest request1 = new IndexRequest(index);
                    // 下载文件 提取文件向量
                    processImage(e);
                    //填充id
                    request1.id(e.getCommonId().toString());
                    commonIds.add(e.getCommonId());
                    //先不修改id
                    request1.source(JSON.toJSONString(e), XContentType.JSON);
                    request1.opType(DocWriteRequest.OpType.INDEX);
                    System.out.println(e.getCommonName()+"导入");
                    indexRequests.add(request1);
                });
                indexRequests.forEach(bulkProcessor::add);
            }
            return ResponseResult.success(1,"数据导入成功");
        } catch (JsonProcessingException e) {
            logger.error("数据导入失败JsonProcessingException",e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("数据导入失败Exception",e);
            e.printStackTrace();
        }
        return ResponseResult.success(1,"数据导入失败");
    }
    /*
     * 删除所有商品
     * */
    public static ResponseResult deleteAll(){
        try {
            //1.创建 SearchRequest搜索请求
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(index);
            //2.创建 SearchSourceBuilder条件构造。
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            searchSourceBuilder.query(matchAllQueryBuilder);
            //设置分页
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(1000);
            //3.将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //4.执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> map = hit.getSourceAsMap();
                DeleteRequest deleteRequest=new DeleteRequest();
                deleteRequest.index(index);
                deleteRequest.id(map.get("commonId").toString());
                client.delete(deleteRequest,RequestOptions.DEFAULT);
                System.out.println("文档"+map.get("commonId")+"已删除");
            }
            long total = hits.getTotalHits().value;
            if(0 == Long.valueOf(total)){
                return ResponseResult.success(ESCodeEnume.DATA_DELETE_SUCCESS.getCode(), ESCodeEnume.DATA_DELETE_SUCCESS.getMsg());
            }else{
                deleteAll();
            }
            return ResponseResult.success(ESCodeEnume.DATA_DELETE_SUCCESS.getCode(), ESCodeEnume.DATA_DELETE_SUCCESS.getMsg());
        } catch (ElasticsearchParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
    /**
     * 批量  添加 / 修改 商品
     */
    public static ResponseResult addOrUpdate(List<ArticleDTO> list){
        try {
            if(!list.isEmpty()){
                //数据导入
                importData(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INPUT_SUCCESS.getCode(), ESCodeEnume.INPUT_SUCCESS.getMsg());
    }
    /**
     * 批量  添加 / 修改 店铺
     */
    public static ResponseResult addOrUpdateShop(List<ArticleShopDTO> list){
        try {
            if(list.size()>0){
                importShopData(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INPUT_SUCCESS.getCode(), ESCodeEnume.INPUT_SUCCESS.getMsg());
    }
    /**
     * 批量 删除 商品
     */
    public static ResponseResult delete(List<Integer> list){
        try {
            if(list.size()>0){
                for (Object goodsId : list) {
                    DeleteRequest deleteRequest=new DeleteRequest();
                    deleteRequest.index(index);
                    deleteRequest.id(goodsId.toString());
                    client.delete(deleteRequest,RequestOptions.DEFAULT);
                    System.out.println("文档"+goodsId+"已删除");
                    //删除商品缓存
                    RedisUtil.remove("unimall_pc_goods_commonId_"+goodsId);
                    RedisUtil.remove("unimall_h5_goods_commonId_"+goodsId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
    }
    /**
     * 批量 删除 店铺
     */
    public static ResponseResult deleteShop(List<Integer> list){
        try {
            if(list.size()>0){
                for (Integer shopId : list) {
                    DeleteRequest deleteRequest=new DeleteRequest();
                    deleteRequest.index(index2);
                    deleteRequest.id(shopId.toString());
                    client.delete(deleteRequest,RequestOptions.DEFAULT);
                    System.out.println("文档"+shopId+"已删除");
                    //删除店铺缓存
                    RedisUtil.remove("unimall_pc_shop_shopId_"+shopId);
                    RedisUtil.remove("unimall_h5_shop_shopId_"+shopId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
    }

    /**
     * 搜索商品
     */
    public static List<ArticleVO> searchGoodsData(ListPageQueryParamDTO queryParam, ShopGoodsCatVO goodsCat) throws IOException {
        List<ArticleVO> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest();
        //指定要查询的索引
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        // 从某个字段中查询出某个值
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String goodsName = queryParam.getCommonName();
        boolean goodsNameExists = !StringUtils.isEmpty(goodsName);

        // 店铺id存在 就根据店铺id查询  不存在就只查询普通商品
        if(null != queryParam.getShopId()){
            //精确匹配
            boolQueryBuilder.filter(QueryBuilders.termQuery("shopId", queryParam.getShopId()));
        }else{
            //1普通商品  2供应商商品 3品牌商品
            //精确匹配
            boolQueryBuilder.filter(QueryBuilders.termQuery("goodsType", 1));
        }

        if(queryParam.getBase64Str() != null && StringUtils.isNotBlank(queryParam.getBase64Str())){
            float[] vector = processBase64Image(queryParam.getBase64Str());
            if(null != vector){
                Script script = new Script(
                        ScriptType.INLINE,
                        "painless",
                        "cosineSimilarity(params.queryVector, doc['vector'])",
                        Collections.singletonMap("queryVector", vector));
                // 根据商品属性获取的commonId集合查询
                if((queryParam.getCatId() == null || (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() == 3))
                        && !StringUtils.isEmpty(queryParam.getAttrName()) && !queryParam.getAttrName().equals(";")){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("commonId", queryParam.getCommonIdList()));
                }
                // 根据分类id查询
                if(null != queryParam.getCatIds() && !queryParam.getCatIds().isEmpty()){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("catId", queryParam.getCatIds()));
                }

                // 根据品牌id查询
                if(null != queryParam.getBrandIdList() && !queryParam.getBrandIdList().isEmpty()){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", queryParam.getBrandIdList()));
                }
                // 使用 bool 查询来确保 vector 字段存在
                boolQueryBuilder.must(QueryBuilders.matchAllQuery())
                        .filter(QueryBuilders.existsQuery("vector"));

                FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                        boolQueryBuilder,
                        ScoreFunctionBuilders.scriptFunction(script));

                searchSourceBuilder.query(functionScoreQueryBuilder)
                        // 设置最小分数为 0.8;
                        .minScore(0.8f);
            }else{
                return null;
            }
        }else if(queryParam.getImagePath() != null && StringUtils.isNotBlank(queryParam.getImagePath())){

            String base64Image = convertImageToBase64(queryParam.getImagePath());
            float[] vector = processBase64Image(base64Image);
            if(null != vector){
                Script script = new Script(
                        ScriptType.INLINE,
                        "painless",
                        "cosineSimilarity(params.queryVector, doc['vector'])",
                        Collections.singletonMap("queryVector", vector));
                // 根据商品属性获取的commonId集合查询
                if((queryParam.getCatId() == null || (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() == 3))
                        && !StringUtils.isEmpty(queryParam.getAttrName()) && !queryParam.getAttrName().equals(";")){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("commonId", queryParam.getCommonIdList()));
                }
                // 根据分类id查询
                if(null != queryParam.getCatIds() && !queryParam.getCatIds().isEmpty()){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("catId", queryParam.getCatIds()));
                }

                // 根据品牌id查询
                if(null != queryParam.getBrandIdList() && !queryParam.getBrandIdList().isEmpty()){
                    //精确匹配
                    boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", queryParam.getBrandIdList()));
                }
                // 使用 bool 查询来确保 vector 字段存在
                boolQueryBuilder.must(QueryBuilders.matchAllQuery())
                        .filter(QueryBuilders.existsQuery("vector"));

                FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                        boolQueryBuilder,
                        ScoreFunctionBuilders.scriptFunction(script));

                searchSourceBuilder.query(functionScoreQueryBuilder)
                        // 设置最小分数为 0.8;
                        .minScore(0.8f);
            }else{
                return null;
            }
        }else{
            // 根据商品名称匹配
            if(goodsNameExists) {
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("commonName.chinese", goodsName);
                matchPhraseQueryBuilder.boost(10f);
                boolQueryBuilder.should(matchPhraseQueryBuilder);
                MatchQueryBuilder codeRawMatchQueryBuilder = QueryBuilders.matchQuery("commonName.chinese", goodsName);
                boolQueryBuilder.must(codeRawMatchQueryBuilder);
            }

            // 查询积分商品
            if(queryParam.getIntegralGoods() != null && queryParam.getIntegralGoods() == 0){
                boolQueryBuilder.must(QueryBuilders.termsQuery("shopId", "0"));
            }

            // 根据分类id查询
            if(null != queryParam.getCatIds() && !queryParam.getCatIds().isEmpty()){
                //精确匹配
                boolQueryBuilder.must(QueryBuilders.termsQuery("catId", queryParam.getCatIds()));
            }

            // 根据品牌id查询
            if(null != queryParam.getBrandIdList() && !queryParam.getBrandIdList().isEmpty()){
                //精确匹配
                boolQueryBuilder.must(QueryBuilders.termsQuery("brandId", queryParam.getBrandIdList()));
            }

            // 根据商品属性获取的commonId集合查询
            if((queryParam.getCatId() == null || (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() == 3))
                    && !StringUtils.isEmpty(queryParam.getAttrName()) && !queryParam.getAttrName().equals(";")){
                //精确匹配
                boolQueryBuilder.must(QueryBuilders.termsQuery("commonId", queryParam.getCommonIdList()));
            }

            // 添加区间查询条件
            if(null != queryParam.getMinPrice() && null != queryParam.getMaxPrice()) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery("commonPrice")
                        .gte(queryParam.getMinPrice())
                        // 大于等于
                        .lte(queryParam.getMaxPrice())
                );
            }
            // 设置查询条件
            searchSourceBuilder.query(boolQueryBuilder);
            // 设置高亮
            if(goodsNameExists){
                searchSourceBuilder.highlighter(new HighlightBuilder()
                        .field("commonName.chinese")
                        .requireFieldMatch(true)
                        .preTags("<font style=\"color:red;\">")
                        .postTags("</font>")
                        .numOfFragments(0)
                        .fragmentSize(800000)
                );
            }
        }

        //综合查询  取消排序 因为es默认排序是按搜索内容的匹配度排序的，加了排序会打乱es默认排序，在精确搜索时搜索的内容不会展示在最上面
        if (null == queryParam.getOrderType() || 3 == queryParam.getOrderType()) {
            // 设置按照匹配度评分排序
            searchSourceBuilder.sort("_score");
        //根据评论查询
        } else if (5 == queryParam.getOrderType()) {
            // 评论排序 1降序，2升序 0 不生效
            if (null != queryParam.getCommentOrder() && 0 != queryParam.getCommentOrder()) {
                if (1 == queryParam.getCommentOrder()) {
                    searchSourceBuilder.sort("commonEvaluate", SortOrder.DESC);
                } else {
                    searchSourceBuilder.sort("commonEvaluate", SortOrder.ASC);
                }
            }
            searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        //根据价格查询
        } else if (1 == queryParam.getOrderType()) {
            // 价格排序 1降序，2升序 0 不生效
            if (null != queryParam.getPriceOrder() && 0 != queryParam.getPriceOrder()) {
                if (1 == queryParam.getPriceOrder()) {
                    searchSourceBuilder.sort("commonPrice", SortOrder.DESC);
                } else {
                    searchSourceBuilder.sort("commonPrice", SortOrder.ASC);
                }
            }
            searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        //根据销量查询
        }else if (2 == queryParam.getOrderType()) {
            // 评论排序 1降序，2升序 0 不生效
            if (null != queryParam.getSaleNum() && 0 != queryParam.getSaleNum()) {
                if (1 == queryParam.getSaleNum()) {
                    searchSourceBuilder.sort("commonSalenum", SortOrder.DESC);
                } else {
                    searchSourceBuilder.sort("commonSalenum", SortOrder.ASC);
                }
            }
            searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        }

        //不返回vector字段，
        searchSourceBuilder.fetchSource(null, "vector");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        //取插叙结果
        SearchHits hits = searchResponse.getHits();
        //取查询结果的总记录数
        System.out.println("查询结果总记录条数:   " + (hits.getTotalHits() != null ? hits.getTotalHits().value : ""));
        //查询结果列表
        for (SearchHit searchHit : hits) {
            //打印文档对象，以json格式输出
            System.out.println(searchHit.getSourceAsString());
            Map<String, Object> sourceMap = searchHit.getSourceAsMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (goodsNameExists) {
                //取title高亮显示的结果
                HighlightField field = highlightFields.get("commonName.chinese");
                Text[] fragments = field.getFragments();
                String fragment = fragments[0].toString();
                sourceMap.put("commonName", fragment);
            }
            sourceMap.putIfAbsent("goodsListFirstShow", "0");
            sourceMap.putIfAbsent("levelId", "0");
            ArticleVO goodsVO = JSON.parseObject(JSON.toJSONString(sourceMap), ArticleVO.class);
            list.add(goodsVO);
        }
        return list;
    }
    public static String convertImageToBase64(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            try (InputStream is = url.openStream()) {
                byte[] bytes = readStream(is);
                String s = Base64.getEncoder().encodeToString(bytes);
                if (s.startsWith("data:image/")) {
                    // 找到逗号的位置并截取后面的部分
                    int commaIndex = s.indexOf(',');
                    if (commaIndex != -1) {
                        return s.substring(commaIndex + 1);
                    }
                }
                return s;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static byte[] readStream(InputStream is) {
        try {
            // 实现读取输入流为字节数组的逻辑
            // 可以使用ByteArrayOutputStream来动态扩展字节数组大小
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * pc 高亮显示查询结果 商品
     *
     * @throws Exception
     */
    public static Map<String, Object> pcSeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam) throws Exception {
        //返回值
        Map<String, Object> map = new HashMap<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(index);

            //创建高亮对象
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //指定查的字段
            String highlightFiled = "commonName.chinese";
           //多条件查询
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 从某个字段中查询出某个值
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            String commonName = webGoodsListQueryParam.getCommonName();

            //编号匹配
            MatchQueryBuilder codeMatchQueryBuilder = QueryBuilders.matchQuery("commonName.chinese", commonName);
            MatchQueryBuilder codeRawMatchQueryBuilder = QueryBuilders.matchQuery("commonName.raw", commonName);
            codeRawMatchQueryBuilder.boost(10f);
            boolQueryBuilder.must(codeMatchQueryBuilder).should(codeRawMatchQueryBuilder);
            //根据品牌查询
            if(null != webGoodsListQueryParam.getBrandIdList() && !webGoodsListQueryParam.getBrandIdList().isEmpty()){
                //精确匹配
                boolQueryBuilder.must(QueryBuilders.termsQuery("brandId",webGoodsListQueryParam.getBrandIdList()));
            }
            //根据分类查询
            if(null != webGoodsListQueryParam.getCatId()){
                //精确匹配
                boolQueryBuilder.must(QueryBuilders.termsQuery("catId", webGoodsListQueryParam.getCatId().toString()));
            }
            //根据商品属性查询
            if(null != webGoodsListQueryParam.getAttributeContentIdList() && !webGoodsListQueryParam.getAttributeContentIdList().isEmpty()){
                BoolQueryBuilder attributeContentIdQuery = QueryBuilders.boolQuery();
                for (String s : webGoodsListQueryParam.getAttributeContentIdList()) {
                    attributeContentIdQuery.should(QueryBuilders.matchQuery("attributeContentId", "-" + s + "-"));
                }
                // 将 attributeContentIdQuery 添加到主查询中
                boolQueryBuilder.must(attributeContentIdQuery);
            }
            // 设置查询条件
            searchSourceBuilder.query(boolQueryBuilder);
            //综合查询  取消排序 因为es默认排序是按搜索内容的匹配度排序的，加了排序会打乱es默认排序，在精确搜索时搜索的内容不会展示在最上面
            if (null == webGoodsListQueryParam.getOrderType() || 3 == webGoodsListQueryParam.getOrderType()) {
                // 设置按照匹配度评分排序
                searchSourceBuilder.sort("_score");
            //根据评论查询
            } else if (5 == webGoodsListQueryParam.getOrderType()) {
                // 评论排序 1降序，2升序 0 不生效
                if (null != webGoodsListQueryParam.getCommentOrder() && 0 != webGoodsListQueryParam.getCommentOrder()) {
                    if (1 == webGoodsListQueryParam.getCommentOrder()) {
                        searchSourceBuilder.sort("commonEvaluate", SortOrder.DESC);
                    } else {
                        searchSourceBuilder.sort("commonEvaluate", SortOrder.ASC);
                    }
                }
                searchSourceBuilder.sort("updateTime", SortOrder.DESC);
            //根据价格查询
            } else if (1 == webGoodsListQueryParam.getOrderType()) {
                // 价格排序 1降序，2升序 0 不生效
                if (null != webGoodsListQueryParam.getPriceOrder() && 0 != webGoodsListQueryParam.getPriceOrder()) {
                    if (1 == webGoodsListQueryParam.getPriceOrder()) {
                        searchSourceBuilder.sort("commonPrice", SortOrder.DESC);
                    } else {
                        searchSourceBuilder.sort("commonPrice", SortOrder.ASC);
                    }
                }
                searchSourceBuilder.sort("updateTime", SortOrder.DESC);
            }
            //设置高亮显示的字段
            //为高亮设置字段
            highlightBuilder.field(highlightFiled);
            //多个字段高亮显示，要设置为false
            highlightBuilder.requireFieldMatch(true);
            //设置高亮显示的前缀
            //设置高亮前标签
            highlightBuilder.preTags("<font style=\"color:red;\">");
            //设置高亮显示的后缀
            //设置高亮后标签
            highlightBuilder.postTags("</font>");
            //最大高亮分片数
            highlightBuilder.fragmentSize(800000);
            //从第一片片获取获取高亮片段
            highlightBuilder.numOfFragments(0);
            //执行查询
            searchSourceBuilder.highlighter(highlightBuilder);
//            searchRequest.source(searchSourceBuilder);
            //设置分页
            searchSourceBuilder.from((webGoodsListQueryParam.getPage() - 1) * webGoodsListQueryParam.getLimit());
            searchSourceBuilder.size(webGoodsListQueryParam.getLimit());
            //3.将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //4.执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //取插叙结果
            SearchHits hits = searchResponse.getHits();
            //取查询结果的总记录数
            System.out.println("查询结果总记录条数:   " + hits.getTotalHits().value);
            ArrayList<ArticleDTO> list = new ArrayList<>();
            //查询结果列表
            Iterator<SearchHit> iterator = hits.iterator();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (iterator.hasNext()) {
                SearchHit searchHit = iterator.next();
                //打印文档对象，以json格式输出
                System.out.println(searchHit.getSourceAsString());
                //取文档的属性
                Map<String, Object> source = searchHit.getSourceAsMap();
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                //取title高亮显示的结果
                HighlightField field = highlightFields.get(highlightFiled);
                Text[] fragments = field.getFragments();
                ArticleDTO articleDto = new ArticleDTO();
                articleDto.setCommonId((Integer) source.get("commonId"));
                articleDto.setGoodsId((Integer) source.get("goodsId"));
                articleDto.setCommonName((String) source.get("commonName"));
                articleDto.setCommonPrice((Double) source.get("commonPrice"));
                articleDto.setCommonEvaluate((Integer) source.get("commonEvaluate"));
                articleDto.setCommonSalenum((Integer) source.get("commonSalenum"));
                articleDto.setCommonCollect((Integer) source.get("commonCollect"));
                articleDto.setShopSelfSupport((Integer) source.get("shopSelfSupport"));

                if (fragments != null) {
                    String fragment = fragments[0].toString();
                    articleDto.setCommonName(fragment);
                }
                list.add(articleDto);
            }

            map.put("list", list);
            map.put("count", hits.getTotalHits().value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * pc 高亮显示查询结果 商铺
     * @throws Exception
     */
    public static Map<String,Object>  pcSeachShopData(WebShopListQueryDTO webGoodsListQueryParam) throws Exception {
        //返回值
        Map<String,Object> map = new HashMap<>();
        try {
            //1.创建 SearchRequest搜索请求
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(index2);

            //创建高亮对象
            HighlightBuilder highlightBuilder=new HighlightBuilder();
            //指定查的字段
            String highlightFiled = "shopName.chinese";
            //多条件查询
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 从某个字段中查询出某个值
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            String shopName = webGoodsListQueryParam.getShopName();

            //编号匹配
            MatchQueryBuilder codeMatchQueryBuilder = QueryBuilders.matchQuery("shopName.chinese", shopName);
            MatchQueryBuilder codeRawMatchQueryBuilder = QueryBuilders.matchQuery("shopName.raw", shopName);
            codeRawMatchQueryBuilder.boost(10f);
            queryBuilder.must(codeMatchQueryBuilder).should(codeRawMatchQueryBuilder);
            if(null != webGoodsListQueryParam.getOrderType() && 1 != webGoodsListQueryParam.getOrderType()){
                //根据区域id查询
                if(null != webGoodsListQueryParam.getDistrictId()){
                    queryBuilder.must(QueryBuilders.queryStringQuery(webGoodsListQueryParam.getDistrictId().toString()).defaultField("districtId"));
                }
                //根据收藏量排序
                if(2 == webGoodsListQueryParam.getOrderType()){
                    searchSourceBuilder.sort("shopCollect", SortOrder.ASC);
                }
            }
            //是否自营店铺 1 不是 2 是
            if(null != webGoodsListQueryParam.getShopSelfSupport() && "" != webGoodsListQueryParam.getShopSelfSupport()){
                queryBuilder.must(QueryBuilders.queryStringQuery(webGoodsListQueryParam.getShopSelfSupport()).defaultField("shopSelfSupport"));
            }
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.sort("shopOperatorTime", SortOrder.DESC);
            //设置高亮显示的字段
            //为高亮设置字段
            highlightBuilder.field(highlightFiled);
            //多个字段高亮显示，要设置为false
            highlightBuilder.requireFieldMatch(true);
            //设置高亮显示的前缀
            //设置高亮前标签
            highlightBuilder.preTags("<font style=\"color:red;\">");
            //设置高亮显示的后缀
            //设置高亮后标签
            highlightBuilder.postTags("</font>");
            //最大高亮分片数
            highlightBuilder.fragmentSize(800000);
            //从第一片片获取获取高亮片段
            highlightBuilder.numOfFragments(0);
            //执行查询
            searchSourceBuilder.highlighter(highlightBuilder);
            //设置分页
            searchSourceBuilder.from((webGoodsListQueryParam.getPage() - 1) * webGoodsListQueryParam.getLimit());
            searchSourceBuilder.size(webGoodsListQueryParam.getLimit());
            //3.将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //4.执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            //取插叙结果
            SearchHits hits = searchResponse.getHits();
            //取查询结果的总记录数
            System.out.println("查询结果总记录条数" + hits.getTotalHits().value);
            ArrayList<ArticleShopDTO> list = new ArrayList<>();
            //查询结果列表
            Iterator<SearchHit> iterator = hits.iterator();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (iterator.hasNext()) {
                SearchHit searchHit = iterator.next();
                //打印文档对象，以json格式输出
                System.out.println(searchHit.getSourceAsString());
                //取文档的属性
                Map<String, Object> source = searchHit.getSourceAsMap();
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                //取title高亮显示的结果
                HighlightField field = highlightFields.get(highlightFiled);
                Text[] fragments = field.getFragments();
                ArticleShopDTO articleShopDto = new ArticleShopDTO();
                articleShopDto.setShopId((Integer) source.get("shopId"));
                articleShopDto.setShopName((String) source.get("shopName"));
                articleShopDto.setShopCollect((Integer) source.get("shopCollect"));
                articleShopDto.setDistrictId((Integer) source.get("districtId"));
                articleShopDto.setShopSelfSupport((Boolean) source.get("shopSelfSupport"));
//                articleShopDto.setShopOperatorTime(formatter.parse((String) source.get("shopOperatorTime")));

                if(fragments!=null){
                    String fragment=fragments[0].toString();
                    articleShopDto.setShopName(fragment);
                }
                list.add(articleShopDto);
            }
            map.put("list",list);
            map.put("count",hits.getTotalHits().value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * h5 高亮显示查询结果 商品
     * @throws Exception
     */
    public static Map<String,Object>  h5SeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam) throws Exception {
        //返回值
        Map<String,Object> map = new HashMap<>();
        //1.创建 SearchRequest搜索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定要查询的索引
        searchRequest.indices(index);

        //创建高亮对象
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        //指定查的字段
        String highlightFiled = "commonName.chinese";
        //设置分页
        webGoodsListQueryParam.setPage((webGoodsListQueryParam.getPage() - 1) * webGoodsListQueryParam.getLimit());
        //多条件查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 从某个字段中查询出某个值
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String commonName = webGoodsListQueryParam.getCommonName();

        //按照商品名称匹配
        //分词匹配
        MatchQueryBuilder nameMatchQueryBuilder = QueryBuilders.matchQuery("commonName.chinese", commonName);
        //精准匹配
        MatchQueryBuilder nameRawMatchQueryBuilder = QueryBuilders.matchQuery("commonName.raw", commonName);
        nameRawMatchQueryBuilder.boost(10f);
        boolQueryBuilder.must(nameMatchQueryBuilder).should(nameRawMatchQueryBuilder);
        //根据品牌查询
        if(null != webGoodsListQueryParam.getBrandIdList() && !webGoodsListQueryParam.getBrandIdList().isEmpty()){
//                boolQueryBuilder.must(QueryBuilders.termQuery("brandId",webGoodsListQueryParam.getBrandName()));//精确匹配
            //精确匹配
            boolQueryBuilder.must(QueryBuilders.termsQuery("brandId",webGoodsListQueryParam.getBrandIdList()));
        }
        //根据商品属性查询
        if(null != webGoodsListQueryParam.getAttributeContentIdList() && !webGoodsListQueryParam.getAttributeContentIdList().isEmpty()){
            BoolQueryBuilder attributeContentIdQuery = QueryBuilders.boolQuery();
            for (String s : webGoodsListQueryParam.getAttributeContentIdList()) {
                attributeContentIdQuery.should(QueryBuilders.matchQuery("attributeContentId", "-" + s + "-"));
            }
            boolQueryBuilder.must(attributeContentIdQuery);
        }
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        if(null != webGoodsListQueryParam.getMinPrice() && null ==  webGoodsListQueryParam.getMaxPrice()){
            // 价格区间
            boolQueryBuilder.must(QueryBuilders.rangeQuery("commonPrice").from(webGoodsListQueryParam.getMinPrice().doubleValue()));
        }else if(null == webGoodsListQueryParam.getMinPrice() && null !=  webGoodsListQueryParam.getMaxPrice()){
            // 价格区间
            boolQueryBuilder.must(QueryBuilders.rangeQuery("commonPrice").to(webGoodsListQueryParam.getMaxPrice().doubleValue()));
        }else if(null != webGoodsListQueryParam.getMinPrice()){
            // 价格区间
            boolQueryBuilder.must(QueryBuilders.rangeQuery("commonPrice").from(webGoodsListQueryParam.getMinPrice().doubleValue()).to(webGoodsListQueryParam.getMaxPrice().doubleValue()));
        }
        if(null != webGoodsListQueryParam.getShopSelfSupport() && !webGoodsListQueryParam.getShopSelfSupport().isEmpty()){
            if(webGoodsListQueryParam.getShopSelfSupport().equals("true")){
                //是否 自营  1不是 0 是
                boolQueryBuilder.must(QueryBuilders.queryStringQuery("0").defaultField("shopSelfSupport"));
            }else{
                //是否 自营  1不是 0 是
                boolQueryBuilder.must(QueryBuilders.queryStringQuery("1").defaultField("shopSelfSupport"));
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //综合查询  取消排序 因为es默认排序是按搜索内容的匹配度排序的，加了排序会打乱es默认排序，在精确搜索时搜索的内容不会展示在最上面
        if(null == webGoodsListQueryParam.getOrderType() || 3 == webGoodsListQueryParam.getOrderType()){
            // 设置按照匹配度评分排序
            searchSourceBuilder.sort("_score");
        //评论数查询
        }else if(5 == webGoodsListQueryParam.getOrderType()) {
            // 评论排序 1降序，2升序 0 不生效
            if(null != webGoodsListQueryParam.getCommentOrder() && 0 != webGoodsListQueryParam.getCommentOrder()){
                if(1 == webGoodsListQueryParam.getCommentOrder()){
                    searchSourceBuilder.sort("commonEvaluate", SortOrder.DESC);
                }else{
                    searchSourceBuilder.sort("commonEvaluate", SortOrder.ASC);
                }
            }
            searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        //根据价格排序
        }else if(1 == webGoodsListQueryParam.getOrderType()){
            // 价格排序 1降序，2升序 0 不生效
            if(null != webGoodsListQueryParam.getPriceOrder() && 0 != webGoodsListQueryParam.getPriceOrder()){
                if(1 == webGoodsListQueryParam.getPriceOrder()){
                    searchSourceBuilder.sort("commonPrice", SortOrder.DESC);
                }else{
                    searchSourceBuilder.sort("commonPrice", SortOrder.ASC);
                }
            }
            searchSourceBuilder.sort("updateTime", SortOrder.DESC);
        }

        //设置高亮显示的字段
        //为高亮设置字段
        highlightBuilder.field(highlightFiled);
        //多个字段高亮显示，要设置为false
        highlightBuilder.requireFieldMatch(true);
        //设置高亮显示的前缀
        //设置高亮前标签
        highlightBuilder.preTags("<font style=\"color:red;\">");
        //设置高亮显示的后缀
        //设置高亮后标签
        highlightBuilder.postTags("</font>");
        //最大高亮分片数
        highlightBuilder.fragmentSize(800000);
        //从第一片片获取获取高亮片段
        highlightBuilder.numOfFragments(0);
        //执行查询
        searchSourceBuilder.highlighter(highlightBuilder);
        //设置分页
        searchSourceBuilder.from(webGoodsListQueryParam.getPage());
        searchSourceBuilder.size(webGoodsListQueryParam.getLimit());
        //3.将 SearchSourceBuilder 添加到 SearchRequest中
        searchRequest.source(searchSourceBuilder);
        //4.执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        //取插叙结果
        SearchHits hits = searchResponse.getHits();
        //取查询结果的总记录数
        System.out.println(" :查询结果总记录条数:   " + hits.getTotalHits().value);
        ArrayList<ArticleDTO> list = new ArrayList<>();
        //查询结果列表
        Iterator<SearchHit> iterator = hits.iterator();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next();
            //取文档的属性
            Map<String, Object> source = searchHit.getSourceAsMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            //取title高亮显示的结果
            HighlightField field = highlightFields.get(highlightFiled);
            Text[] fragments = field.getFragments();
            ArticleDTO articleDto = new ArticleDTO();
            articleDto.setCommonId((Integer) source.get("commonId"));
            articleDto.setGoodsId((Integer) source.get("goodsId"));
            articleDto.setCommonName((String) source.get("commonName"));
            articleDto.setCommonPrice((Double) source.get("commonPrice"));
            articleDto.setCommonEvaluate((Integer) source.get("commonEvaluate"));
            articleDto.setCommonSalenum((Integer) source.get("commonSalenum"));
            articleDto.setCommonCollect((Integer) source.get("commonCollect"));
            articleDto.setShopSelfSupport((Integer) source.get("shopSelfSupport"));

            if(fragments!=null){
                String fragment=fragments[0].toString();
                articleDto.setCommonName(fragment);
            }
            list.add(articleDto);
        }
        map.put("list",list);
        map.put("count",hits.getTotalHits().value);
        return map;
    }

    /**
     * h5 高亮显示查询结果  商铺
     * @throws Exception
     */
    public static Map<String,Object>  h5SeachShopData(ShopListQueryDTO shopListQueryParam) throws Exception {
        //返回值
        Map<String,Object> map = new HashMap<>();
        //1.创建 SearchRequest搜索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定要查询的索引
        searchRequest.indices(index2);

        //创建高亮对象
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        //指定查的字段
        String highlightFiled = "shopName.chinese";
        //设置分页
        shopListQueryParam.setPage((shopListQueryParam.getPage() - 1) * shopListQueryParam.getLimit());
        //多条件查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 从某个字段中查询出某个值
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        String shopName = shopListQueryParam.getShopName();
//            queryBuilder.must(QueryBuilders.queryStringQuery(shopName).defaultField("shopName"));//根据商品名称查询
        //编号匹配
        MatchQueryBuilder codeMatchQueryBuilder = QueryBuilders.matchQuery("shopName.chinese", shopName);
        MatchQueryBuilder codeRawMatchQueryBuilder = QueryBuilders.matchQuery("shopName.raw", shopName);
        codeRawMatchQueryBuilder.boost(10f);
        queryBuilder.must(codeMatchQueryBuilder).should(codeRawMatchQueryBuilder);
        // 设置查询条件
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.sort("shopOperatorTime", SortOrder.DESC);
        //设置高亮显示的字段
        //为高亮设置字段
        highlightBuilder.field(highlightFiled);
        //多个字段高亮显示，要设置为false
        highlightBuilder.requireFieldMatch(true);
        //设置高亮显示的前缀
        //设置高亮前标签
        highlightBuilder.preTags("<font style=\"color:red;\">");
        //设置高亮显示的后缀
        //设置高亮后标签
        highlightBuilder.postTags("</font>");
        //最大高亮分片数
        highlightBuilder.fragmentSize(800000);
        //从第一片片获取获取高亮片段
        highlightBuilder.numOfFragments(0);
        //执行查询
        searchSourceBuilder.highlighter(highlightBuilder);
        //设置分页
        searchSourceBuilder.from(shopListQueryParam.getPage());
        searchSourceBuilder.size(shopListQueryParam.getLimit());
        //3.将 SearchSourceBuilder 添加到 SearchRequest中
        searchRequest.source(searchSourceBuilder);
        //4.执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        //取插叙结果
        SearchHits hits = searchResponse.getHits();
        //取查询结果的总记录数
        System.out.println("查询结果总记录条数" + hits.getTotalHits().value);
        ArrayList<ArticleShopDTO> list = new ArrayList<>();
        //查询结果列表
        Iterator<SearchHit> iterator = hits.iterator();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next();
            //打印文档对象，以json格式输出
            System.out.println(searchHit.getSourceAsString());
            //取文档的属性
            Map<String, Object> source = searchHit.getSourceAsMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            //取title高亮显示的结果
            HighlightField field = highlightFields.get(highlightFiled);
            Text[] fragments = field.getFragments();
            ArticleShopDTO articleShopDto = new ArticleShopDTO();
            articleShopDto.setShopId((Integer) source.get("shopId"));
            articleShopDto.setShopName((String) source.get("shopName"));
            articleShopDto.setShopCollect((Integer) source.get("shopCollect"));
            articleShopDto.setDistrictId((Integer) source.get("districtId"));
            articleShopDto.setShopSelfSupport((Boolean) source.get("shopSelfSupport"));

            if (fragments != null) {
                String fragment = fragments[0].toString();
                articleShopDto.setShopName(fragment);
            }
            list.add(articleShopDto);
        }
        map.put("list", list);
        map.put("count", hits.getTotalHits().value);
        return map;
    }

    /**
     * 创建店铺索引库
     *
     * @throws Exception
     */
    public static ResponseResult setShopMappings() {
        try {
            //使用client对象创建一个索引库
            //参数1：创建索引请求对象 参数2：请求配置对象
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index2);

            //创建一个Mappings信息
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    //店铺id
                    .startObject("shopId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //店铺名称
                    .startObject("shopName")
                    .field("type", "text")
                    .field("store", true)
                    .startObject("fields")
                    //精准匹配字段
                    .startObject("raw")
                    .field("type", "keyword")
                    .endObject()
                    //分词匹配字段
                    .startObject("chinese")
                    .field("type", "text")
//                    .field("analyzer", "ik_max_word")
//                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .endObject()
                    .endObject()
                    //店铺粉丝数
                    .startObject("shopCollect")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //地区id
                    .startObject("districtId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()
                    //是否自营商品 true 是 false 否
                    .startObject("shopSelfSupport")
                    .field("type", "boolean")
                    .field("store", true)
                    .endObject()
                    //审核时间
                    .startObject("shopOperatorTime")
                    .field("type", "date")
                    .field("store", true)
                    .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                    .endObject()
                    .endObject()
                    .endObject();
            createIndexRequest.mapping(builder);
            //使用client把mapping信息设置到索引库中
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            return ResponseResult.success(ESCodeEnume.INDEX_CREATE_SUCCESS.getCode(), ESCodeEnume.INDEX_CREATE_SUCCESS.getMsg());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INDEX_CREATE_FAIL.getCode(), ESCodeEnume.INDEX_CREATE_FAIL.getMsg());
    }
    public static ResponseResult deleteShopIndex() {
        boolean acknowledged = false;
        try {
            //创建删除索引请求
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index2);
            //        执行
            AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //得到相应
            acknowledged = delete.isAcknowledged();
            if(acknowledged){
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_SUCCESS.getCode(), ESCodeEnume.INDEX_DELETE_SUCCESS.getMsg());
            }else{
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_FAIL.getCode(), ESCodeEnume.INDEX_DELETE_FAIL.getMsg());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.success("删除索引库失败");
    }
    /** 批量导入店铺 */
    public static ResponseResult importShopData(List<ArticleShopDTO> articleShopDTOS){
        try {
            //先判断索引库是否存在 不存在 就创建
            GetIndexRequest request = new GetIndexRequest();
            boolean exists = client.indices().exists(request.indices(index2), RequestOptions.DEFAULT);
            if(exists) {
                System.out.println("店铺索引库存在");
            }else{
                System.out.println("店铺索引库不存在");
                ResponseResult resp = setShopMappings();
                System.out.println(resp.getMessage());
            }
            List<Integer> shopIds = new ArrayList<>();
            List<IndexRequest> indexRequests = new ArrayList<>();
            if(null != articleShopDTOS && articleShopDTOS.size() > 0){
                articleShopDTOS.forEach(e -> {
                    IndexRequest request1 = new IndexRequest(index2);
                    //填充id
                    request1.id(e.getShopId().toString());
                    shopIds.add(e.getShopId());
                    //先不修改id
                    request1.source(JSON.toJSONString(e), XContentType.JSON);
                    request1.opType(DocWriteRequest.OpType.INDEX);
                    System.out.println(e.getShopName()+"导入");
                    indexRequests.add(request1);
                });
                indexRequests.forEach(bulkProcessor::add);
            }
            return ResponseResult.success(ESCodeEnume.INPUT_SUCCESS.getCode(), ESCodeEnume.INPUT_SUCCESS.getMsg());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INPUT_FAIL.getCode(), ESCodeEnume.INPUT_FAIL.getMsg());
    }
    /*
     * 删除所有店铺
     * */
    public static ResponseResult deleteShopAll(){
        try {
            //1.创建 SearchRequest搜索请求
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(index2);
            //2.创建 SearchSourceBuilder条件构造。
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            searchSourceBuilder.query(matchAllQueryBuilder);
            //设置分页
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(1000);

            //3.将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //4.执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> map = hit.getSourceAsMap();
                System.out.println(map);
                DeleteRequest deleteRequest=new DeleteRequest();
                deleteRequest.index(index2);
                deleteRequest.id(map.get("shopId").toString());
                client.delete(deleteRequest,RequestOptions.DEFAULT);
                System.out.println("文档"+map.get("shopId")+"已删除");
            }
            long total = hits.getTotalHits().value;
            if(0 == Long.valueOf(total)){
                return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
            }else{
                deleteShopAll();
            }
            return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
        } catch (ElasticsearchParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

    private static BulkProcessor createBulkProcessor() {

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.info("1. 【beforeBulk】批次[{}] 携带 {} 请求数量", executionId, request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                if (!response.hasFailures()) {
                    logger.info("2. 【afterBulk-成功】批量 [{}] 完成在 {} ms", executionId, response.getTook().getMillis());
                } else {
                    BulkItemResponse[] items = response.getItems();
                    for (BulkItemResponse item : items) {
                        if (item.isFailed()) {
                            logger.info("2. 【afterBulk-失败】批量 [{}] 出现异常的原因 : {}", executionId, item.getFailureMessage());
                            break;
                        }
                    }
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {

                List<DocWriteRequest<?>> requests = request.requests();
                List<String> esIds = requests.stream().map(DocWriteRequest::id).collect(Collectors.toList());
                logger.error("3. 【afterBulk-failure失败】es执行bluk失败,失败的esId为：{}", esIds, failure);
            }
        };

        BulkProcessor.Builder builder = BulkProcessor.builder(((bulkRequest, bulkResponseActionListener) -> {
            client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, bulkResponseActionListener);
        }), listener);
        //到达10000条时刷新
        builder.setBulkActions(10000);
        //内存到达8M时刷新
        builder.setBulkSize(new ByteSizeValue(8L, ByteSizeUnit.MB));
        //设置的刷新间隔10s
        builder.setFlushInterval(TimeValue.timeValueSeconds(10));
        //设置允许执行的并发请求数。
        builder.setConcurrentRequests(8);
        //设置重试策略
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1), 3));
        return builder.build();
    }

    public static Map<String, Object> querySeachBrandAttributeData(WebGoodsListQueryDTO webGoodsListQueryParam) {
        //返回值
        Map<String,Object> map = new HashMap<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(index);

            //多条件查询
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 从某个字段中查询出某个值
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            String commonName = webGoodsListQueryParam.getCommonName();
            //编号匹配
            MatchQueryBuilder codeMatchQueryBuilder = QueryBuilders.matchQuery("commonName.chinese", commonName);
            MatchQueryBuilder codeRawMatchQueryBuilder = QueryBuilders.matchQuery("commonName.raw", commonName);
            codeRawMatchQueryBuilder.boost(10f);
            boolQueryBuilder.must(codeMatchQueryBuilder).should(codeRawMatchQueryBuilder);
            // 设置查询条件
            searchSourceBuilder.query(boolQueryBuilder);
            //将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            //取插叙结果
            SearchHits hits = searchResponse.getHits();
            //查询结果列表
            Iterator<SearchHit> iterator = hits.iterator();
            //存放品牌
            LinkedHashSet<Map<String,Object>> goodsBrandCatParam = new LinkedHashSet<>();
            LinkedHashSet<Map<String, Object>> mapList = new LinkedHashSet<>();
            //存放catId
            LinkedHashSet<String> catIds = new LinkedHashSet<>();
            while (iterator.hasNext()) {
                SearchHit searchHit = iterator.next();
                //打印文档对象，以json格式输出
                System.out.println(searchHit.getSourceAsString());
                //取文档的属性
                Map<String, Object> source = searchHit.getSourceAsMap();
                if(null != source.get("brandId") && "" != source.get("brandId")
                        && null != source.get("brandName") && "" != source.get("brandName") ){
                    //保存商品的品牌
                    Map<String,Object> brandmap = new HashMap<>();
                    brandmap.put("brandId",source.get("brandId"));
                    brandmap.put("brandName",source.get("brandName"));
                    goodsBrandCatParam.add(brandmap);
                }
                //存放catIds
                catIds.add((String) source.get("catId"));
            }
            //根据分类id 查询商品分类属性
            if(!catIds.isEmpty()){
                mapList = queryCatAttribute(catIds);
            }
            map.put("gdbGoodsAttParams",mapList);
            map.put("goodsBrandCatParam",goodsBrandCatParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static  LinkedHashSet<Map<String,Object>> queryCatAttribute(LinkedHashSet<String> catIds){
        //商品属性查询
        LinkedHashSet<Map<String,Object>> goodsAttributeResults= esGoodsCommonAttributeAssociationMapper.esSelectAttributes(catIds);
        if(null != goodsAttributeResults && goodsAttributeResults.size() > 0) {
            //根据商品属性id查询属性内容
            for (Map<String,Object> goodsAttribute : goodsAttributeResults) {
                LinkedHashSet<Map<String,Object>> goodsAttributeContent =  esGoodsCommonAttributeAssociationMapper.esSelectAttContents((Integer) goodsAttribute.get("id"));
                goodsAttribute.put("goodsAttributeContentResults",goodsAttributeContent);
            }
        }
        return goodsAttributeResults;
    }
    /**
     * 创建敏感词索引库
     * @throws Exception
     */
    public static ResponseResult createSensitiveIndex(){
        try {
            //使用client对象创建一个索引库
            //参数1：创建索引请求对象 参数2：请求配置对象
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(sensitiveIndex);

            //创建一个Mappings信息
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    //敏感词id
                    .startObject("id")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()

                    //敏感词
                    .startObject("sensitiveName")
                    .field("type", "keyword")
                    .endObject()

                    //敏感词分类名称
                    .startObject("classificationName")
                    .field("type", "text")
                    .field("store", true)
                    .endObject()


                    //敏感词分类id
                    .startObject("sensitiveClassificationId")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()


                    //敏感词 状态 1启用  0 不启用
                    .startObject("sensitiveState")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()


                    //敏感词分类 状态 1启用  0 不启用
                    .startObject("classificationState")
                    .field("type", "integer")
                    .field("store", true)
                    .endObject()

                    //添加时间
                    .startObject("sensitiveAddTime")
                    .field("type", "date")
                    .field("store", true)
                    .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                    .endObject()


                    .endObject()
                    .endObject();
            createIndexRequest.mapping(builder);
            //使用client把mapping信息设置到索引库中
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            return ResponseResult.success(ESCodeEnume.INDEX_CREATE_SUCCESS.getCode(), ESCodeEnume.INDEX_CREATE_SUCCESS.getMsg());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INDEX_CREATE_FAIL.getCode(), ESCodeEnume.INDEX_CREATE_FAIL.getMsg());
    }

    public static ResponseResult deleteSensitiveIndex() {
        boolean acknowledged = false;
        try {
            //创建删除索引请求
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(sensitiveIndex);
            //执行
            AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //得到相应
            acknowledged = delete.isAcknowledged();
            if(acknowledged){
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_SUCCESS.getCode(), ESCodeEnume.INDEX_DELETE_SUCCESS.getMsg());
            }else{
                return ResponseResult.success(ESCodeEnume.INDEX_DELETE_FAIL.getCode(), ESCodeEnume.INDEX_DELETE_FAIL.getMsg());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INDEX_DELETE_FAIL.getCode(), ESCodeEnume.INDEX_DELETE_FAIL.getMsg());
    }
    /** 批量导入敏感词 */
    public static ResponseResult importSensitiveData(List<BaseSensitiveVO> baseSensitiveVOList){
        try {
            //先判断索引库是否存在 不存在 就创建
            GetIndexRequest request = new GetIndexRequest();
            boolean exists = client.indices().exists(request.indices(sensitiveIndex), RequestOptions.DEFAULT);
            if(exists) {
                System.out.println("敏感词索引库存在");
            }else{
                System.out.println("敏感词索引库不存在");
                ResponseResult resp = createSensitiveIndex();
                System.out.println(resp.getMessage());
            }
            List<Integer> sensitiveIds = new ArrayList<>();
            List<IndexRequest> indexRequests = new ArrayList<>();
            if(null != baseSensitiveVOList && baseSensitiveVOList.size() > 0){
                baseSensitiveVOList.forEach(e -> {
                    IndexRequest request1 = new IndexRequest(sensitiveIndex);
                    //填充id
                    request1.id(e.getId().toString());
                    sensitiveIds.add(e.getId());
                    //先不修改id
                    request1.source(JSON.toJSONString(e), XContentType.JSON);
                    request1.opType(DocWriteRequest.OpType.INDEX);
                    System.out.println(e.getSensitiveName()+"导入");
                    indexRequests.add(request1);
                });
                indexRequests.forEach(bulkProcessor::add);
            }
            return ResponseResult.success(ESCodeEnume.INPUT_SUCCESS.getCode(), ESCodeEnume.INPUT_SUCCESS.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INPUT_FAIL.getCode(), ESCodeEnume.INPUT_FAIL.getMsg());
    }
    /*
     * 删除敏感词
     * */
    public static ResponseResult deleteSensitiveAll(){
        try {
            //1.创建 SearchRequest搜索请求
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(sensitiveIndex);
            //2.创建 SearchSourceBuilder条件构造。
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            searchSourceBuilder.query(matchAllQueryBuilder);
            //设置分页
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(1000);

            //3.将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //4.执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> map = hit.getSourceAsMap();
                System.out.println(map);
                DeleteRequest deleteRequest=new DeleteRequest();
                deleteRequest.index(sensitiveIndex);
                deleteRequest.id(map.get("id").toString());
                client.delete(deleteRequest,RequestOptions.DEFAULT);
                System.out.println("文档"+map.get("id")+"已删除");
            }
            long total = hits.getTotalHits().value;
            if(0 == Long.valueOf(total)){
                return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
            }else{
                deleteSensitiveAll();
            }
            return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
        } catch (ElasticsearchParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

    /**
     * 批量  添加 / 修改 敏感词
     */
    public static ResponseResult addOrUpdateSensitive(List<BaseSensitiveVO> list){
        try {
            if(list.size()>0){
                importSensitiveData(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.INPUT_SUCCESS.getCode(), ESCodeEnume.INPUT_SUCCESS.getMsg());
    }
    /**
     * 批量 删除 敏感词
     */
    public static ResponseResult deleteSensitive(List<Integer> list){
        try {
            if(list.size()>0){
                for (Object goodsId : list) {
                    DeleteRequest deleteRequest=new DeleteRequest();
                    deleteRequest.index(sensitiveIndex);
                    deleteRequest.id(goodsId.toString());
                    client.delete(deleteRequest,RequestOptions.DEFAULT);
                    System.out.println("文档"+goodsId+"已删除");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(ESCodeEnume.DELETE_SUCCESS.getCode(), ESCodeEnume.DELETE_SUCCESS.getMsg());
    }

    public static ResponseResult esSeachSensitive(BaseSensitiveDTO baseSensitiveDTO)
            throws Exception {
        HashSet<String> list = new HashSet<String>();
        //封装请求参数 调用es进行分词
        Request request = new Request("GET", "_analyze");
        JSONObject entity = new JSONObject();
        entity.put("analyzer", "ik_max_word");
//        entity.put("analyzer", "ik_smart");
        entity.put("text", baseSensitiveDTO.getSensitiveName());
        request.setJsonEntity(entity.toJSONString());
        Response response = client.getLowLevelClient().performRequest(request);
        JSONObject tokens = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
        JSONArray arrays = tokens.getJSONArray("tokens");
        for (int i = 0; i < arrays.size(); i++) {
            JSONObject obj = JSON.parseObject(arrays.getString(i));
            list.add(obj.getString("token"));
        }
        if(null != list && list.size() > 0){
            Map<String, Object> stringObjectMap = esQuerySensitive(list);
            HashSet<String> strings = (HashSet<String>) stringObjectMap.get("resultList");
            HashSet<BaseSensitiveVO> resultBaseSensitive = (HashSet<BaseSensitiveVO>) stringObjectMap.get("resultBaseSensitive");
            if(strings.isEmpty()){
                return ResponseResult.success();
            }else{
                //存在敏感词 写入命中记录
                if(null != baseSensitiveDTO.getToken()){
                    //封装敏感词信息
                    BaseSensitiveRecordDTO baseSensitiveRecordDTO = new BaseSensitiveRecordDTO();
                    baseSensitiveRecordDTO.setSensitiveTxt(baseSensitiveDTO.getSensitiveName());
                    baseSensitiveRecordDTO.setRecordAddTime(new Date());

                    String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + baseSensitiveDTO.getToken());
                    String platfomUserinfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + baseSensitiveDTO.getToken());
                    if(null != userInfoStr){
                        //封装用户信息
                        UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
                        baseSensitiveRecordDTO.setUserId(dto.getUser().getUserId());
                        baseSensitiveRecordDTO.setUserName(dto.getUser().getUserMobile());
                        baseSensitiveRecordDTO.setUserType(CommonType.UserType.REGULAR_USERS.getCode());
                    }else if(null != platfomUserinfoStr){
                        UserInfoDTO dto = JSON.parseObject(platfomUserinfoStr, UserInfoDTO.class);
                        baseSensitiveRecordDTO.setUserId(dto.getUser().getUserId());
                        baseSensitiveRecordDTO.setUserName(dto.getUser().getUserAccount());
                        baseSensitiveRecordDTO.setUserType(CommonType.UserType.PLATFOM_USERS.getCode());
                    }
                    //插入敏感词命中记录
                    for (BaseSensitiveVO baseSensitiveVO : resultBaseSensitive) {
                        baseSensitiveRecordDTO.setClassificationId(baseSensitiveVO.getSensitiveClassificationId());
                        baseSensitiveRecordDTO.setSensitiveName(baseSensitiveVO.getSensitiveName());
                        esBaseServiceClient.addbaseSensitiveRecord(baseSensitiveRecordDTO);
                    }
                }
                return ResponseResult.success(BizCodeEnume.EXIST_SENSITIVE.getCode(),BizCodeEnume.EXIST_SENSITIVE.getMsg(),strings);
            }
        }
        return ResponseResult.success();
    }
    public static Map<String,Object> esQuerySensitive(HashSet<String> list) {
        //存放敏感词
        HashSet<String> resultList = new HashSet<String>();
        HashSet<BaseSensitiveVO> resultBaseSensitive = new HashSet<BaseSensitiveVO>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            //指定要查询的索引
            searchRequest.indices(sensitiveIndex);

            //多条件查询
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 从某个字段中查询出某个值
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //精确匹配
            boolQueryBuilder.must(QueryBuilders.termsQuery("sensitiveName",list));

            // 设置查询条件
            searchSourceBuilder.query(boolQueryBuilder);
            //将 SearchSourceBuilder 添加到 SearchRequest中
            searchRequest.source(searchSourceBuilder);
            //执行查询
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //取插叙结果
            SearchHits hits = searchResponse.getHits();
            //查询结果列表
            for (SearchHit searchHit : hits) {
                //打印文档对象，以json格式输出
                System.out.println(searchHit.getSourceAsString());
                //取文档的属性
                Map<String, Object> source = searchHit.getSourceAsMap();
                BaseSensitiveVO baseSensitiveVO = new BaseSensitiveVO();
                baseSensitiveVO.setId(Integer.valueOf(source.get("id").toString()));
                baseSensitiveVO.setSensitiveName(source.get("sensitiveName").toString());
                baseSensitiveVO.setSensitiveClassificationId(Integer.valueOf(source.get("sensitiveClassificationId").toString()));
                resultBaseSensitive.add(baseSensitiveVO);
                resultList.add(source.get("sensitiveName").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,Object> map = new HashMap<>();
        map.put("resultList",resultList);
        map.put("resultBaseSensitive",resultBaseSensitive);
        return map;
    }
    /**
     * 创建临时图片
     * 该方法根据给定的图片URL，下载图片并保存为临时文件
     * 主要用于需要临时处理图片的场景，提供了一种快速获取图片的方式
     *
     * @param imageUrl 图片的URL地址
     * @return 下载的临时图片文件，如果下载失败或发生异常，则返回null
     * @throws Exception 当图片下载过程中发生错误时抛出异常
     */
    private static File downloadImage(String imageUrl) {
        File tempFile = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            // 记录图片下载开始的日志
            logger.info("开始下载图片: {}", imageUrl);
            // 创建URL对象，用于打开图片的输入流
            URL url = new URL(imageUrl);
            is = url.openStream();
            // 检查输入流是否为空，如果为空则记录错误日志并返回null
            if (is == null) {
                logger.error("无法打开图片URL的输入流: {}", imageUrl);
                return null;
            }
            // 创建临时文件，用于保存下载的图片
            tempFile = File.createTempFile("Imagetemp", ".jpg");
            fos = new FileOutputStream(tempFile);

            // 使用缓冲区读取图片数据并写入临时文件
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            // 记录图片下载成功的日志
            logger.info("图片下载成功: {}", tempFile.getAbsolutePath());
            // 返回下载的临时图片文件
            return tempFile;
        } catch (Exception e) {
            // 记录图片下载失败的日志
            logger.error("图片下载失败: {}", imageUrl, e);
        } finally {
            // 关闭输入流和输出流，释放系统资源
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // 记录关闭流时发生的错误日志
                logger.error("关闭流时发生错误: {}", imageUrl, e);
            }
        }
        // 如果发生异常或错误，返回null
        return null;
    }

    /**
     * 提取图片向量  并删除临时文件
     * @param articleDTO 包含图片URL的ArticleDTO对象
     * @return 处理后的ArticleDTO对象，包含图片向量
     */
    private static ArticleDTO processImage(ArticleDTO articleDTO) {
        try {
            // 获取图片URL
            String imageUrl = articleDTO.getCommonImage();
            // 下载图片到临时文件
            File file = downloadImage(imageUrl);
            // 创建文件输入流
            InputStream fileInputStream = new FileInputStream(file);
            try {
                // 从输入流中读取图片
                Image image = ImageFactory.getInstance().fromInputStream(fileInputStream);
                // 如果图片为空，则记录错误并返回原始对象
                if (image == null) {
                    logger.error("无法从输入流读取图片: {}", articleDTO.getCommonId());
                    return articleDTO;
                }
                // 提取文件向量
                float[] vector = predictor.predict(image);
                // 将向量设置到ArticleDTO对象中
                articleDTO.setVector(vector);
                // 记录图片处理成功的日志
                logger.info("图片处理成功: {}", articleDTO.getCommonId());
            } catch (Exception e) {
                // 记录图片处理异常的日志
                logger.error("处理图片时发生异常: {}", articleDTO.getCommonId(), e);
                // 抛出异常
                throw e;
            } finally {
                // 关闭文件输入流
                fileInputStream.close();
                // 删除临时文件
                Files.deleteIfExists(file.toPath());
            }
        } catch (Exception e) {
            // 记录图片处理异常的日志
            logger.error("处理图片时发生异常: {}", articleDTO.getCommonId(), e);
            // 返回原始对象
            return articleDTO;
        }
        // 返回处理后的ArticleDTO对象
        return articleDTO;
    }

    /**
     * 下载模型文件
     * 当模型文件不存在时，需要联网下载，模型文件大小为98M
     *
     * @param modelPath 模型文件的保存路径
     * @throws Exception 如果下载过程中发生错误，则抛出异常
     */
    private static void downloadModel(Path modelPath) throws Exception {
        // 记录日志：提示模型文件需要联网下载
        logger.info("图片模型不存在需联网下载 模型大小98M");
        // 记录日志：开始下载模型文件
        logger.info("图片模型下载开始");

        // 创建模型文件下载的URL对象
        URL url = new URL(MODEL_URL);
        // 打开URL连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置请求方法为GET
        connection.setRequestMethod("GET");

        // 获取服务器响应码
        int responseCode = connection.getResponseCode();
        // 如果响应码不是200，表示下载失败，抛出异常
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download model. Server returned HTTP " + responseCode);
        }

        // 使用try-with-resources语句确保输入流和输出流在使用后能被正确关闭
        try (InputStream inputStream = connection.getInputStream();
             OutputStream outputStream = new FileOutputStream(modelPath.toFile())) {

            // 创建缓冲区，用于读取和写入数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            // 循环读取数据并写入文件，直到输入流结束
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // 记录日志：模型文件下载结束
        logger.info("图片模型下载结束");
    }
    /**
     * 提取 Base64 图片向量 并删除临时文件
     * @param base64Image Base64 编码的图片字符串
     */
    private static float[] processBase64Image(String base64Image) {
        try {
            // 去掉 Base64 数据 URL 前缀（如果有的话）
            if (base64Image.startsWith("data:image/")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            // 解码 Base64 字符串为字节数组
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 将字节数组转换为 InputStream
            InputStream fileInputStream = new ByteArrayInputStream(imageBytes);

            // 使用 ImageFactory 从 InputStream 创建 Image 对象
            Image image = ImageFactory.getInstance().fromInputStream(fileInputStream);
            if (image == null) {
                logger.error("无法从输入流读取图片: ");
                return null;
            }

            // 提取文件向量
            float[] vector = predictor.predict(image);
            logger.info("Base64 图片处理成功: ");
            return vector;
        } catch (Exception e) {
            logger.error("处理 Base64 图片时发生异常:", e);
            return null;
        }
    }
}
