package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.dto.CategoryMonthlySalesDTO;
import com.houjiang.job.entity.JobStatisticsCatEntity;
import com.houjiang.job.mapper.JobStatisticsCatMapper;
import com.houjiang.job.service.JobStatisticsCatService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Amos
 */
@Service
public class JobStatisticsCatServiceImpl extends ServiceImpl<JobStatisticsCatMapper, JobStatisticsCatEntity>
    implements JobStatisticsCatService {

    /**
     * 插入统计分类销售数据。
     *
     * @param param 包含分类月度销售数据的DTO对象。
     * @return 返回操作结果，成功与否。
     */
    @Override
    public ResponseResult insertOdbStatisticsCat(CategoryMonthlySalesDTO param) {
        // 查询实际销售数据
        List<CategoryMonthlySalesDTO> realList = this.baseMapper.queryCatTurnoverReal(param);
        // 查询自定义销售数据
        List<CategoryMonthlySalesDTO> selfList = this.baseMapper.queryCatTurnoverSelf(param);

        // 遍历实际销售数据和自定义销售数据，匹配并合并数据
        for (CategoryMonthlySalesDTO real : realList) {
            for (CategoryMonthlySalesDTO self : selfList) {
                // 如果找到匹配的分类ID
                if (real.getFirstCatId().equals(self.getFirstCatId())) {
                    // 设置自定义销售金额
                    real.setCatTurnoverSelf(self.getCatTurnoverSelf());
                    // 根据分类ID设置分类名称
                    if (real.getFirstCatId() != null) {
                        switch (real.getFirstCatId()) {
                            case 999:
                                real.setFirstCatName("其它分类");
                                break;
                            case 1000:
                                real.setFirstCatName("地方特产");
                                break;
                            case 1006080236:
                                real.setFirstCatName("粮油调味");
                                break;
                            case 1006080309:
                                real.setFirstCatName("乳品冲饮");
                                break;
                            case 1006080325:
                                real.setFirstCatName("茗茶名酒");
                                break;
                            case 1006080506:
                                real.setFirstCatName("休闲食品");
                                break;
                            case 1006080654:
                                real.setFirstCatName("滋补营养");
                                break;
                            case 1006080730:
                                real.setFirstCatName("生鲜果蔬");
                                break;
                        }
                    }
                    // 计算总销售金额
                    if (self.getCatTurnoverSelf() != null) {
                        real.setCatTurnoverAll(real.getCatTurnoverReal().add(self.getCatTurnoverSelf()));
                    }

                    // 创建统计分类实体对象，并复制DTO数据到实体对象
                    JobStatisticsCatEntity entity=new JobStatisticsCatEntity();
                    BeanUtils.copyProperties(real,entity);
                    // 插入统计分类实体到数据库
                    this.baseMapper.insert(entity);
                }
            }
        }

        // 返回操作成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
    }
}




