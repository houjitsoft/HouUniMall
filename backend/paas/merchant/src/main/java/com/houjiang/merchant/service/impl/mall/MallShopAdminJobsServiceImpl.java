package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopAdminJob;
import com.houjiang.merchant.common.entity.mall.MallShopJob;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminJobMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopJobMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.service.mall.IMallShopAdminJobsService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统岗位实现类
 * @author Amos
 */
@Service
public class MallShopAdminJobsServiceImpl implements IMallShopAdminJobsService {

    @Resource
    MallShopAdminJobMapper mallShopAdminJobMapper;
    @Resource
    MallShopJobMapper mallShopJobMapper;
    @Override
    public List<Integer> getJobIdAttr(Integer adminId) {
        List<Integer> ret = new ArrayList<Integer>();
        List<MallShopAdminJob> rolesList = mallShopAdminJobMapper.selectList(new QueryWrapper<MallShopAdminJob>().eq("admin_id", adminId).select("job_id"));
        if (rolesList.size() > 0) {
            for (MallShopAdminJob adminJobs : rolesList) {
                if (ret.contains(adminJobs.getJobId()) == false) {
                    ret.add(adminJobs.getJobId());
                }
            }
        }
        return ret;
    }

    @Override
    public List<String> getJobNameAttr(Integer adminId) {
        List<String> ret = new ArrayList<String>();
        List<Integer> ids = getJobIdAttr(adminId);
        if (ids.size() == 0) {
            return ret;
        } else {
            List<MallShopJob> jobsList = mallShopJobMapper.selectList(new QueryWrapper<MallShopJob>().in("id", ids).select("name"));
            if (jobsList.size() > 0) {
                for (MallShopJob job : jobsList) {
                    ret.add(job.getName());
                }
            }
            return ret;
        }
    }

    @Override
    public void batchInsert(Integer adminId, List<Integer> jobsIds, Integer shopId) {
        this.deleteByAdminId(adminId);
        if (jobsIds != null && !jobsIds.isEmpty()) {
            jobsIds.forEach(item-> {
                mallShopAdminJobMapper.insert(new MallShopAdminJob(){{
                    setAdminId(adminId);
                    setJobId(item);
                    setShopId(shopId);
                    setCreateTime(System.currentTimeMillis() / 1000);
                    setUpdateTime(System.currentTimeMillis() / 1000);
                }});
            });
        }
    }

    @Override
    public void deleteByAdminId(Integer adminId) {
        mallShopAdminJobMapper.delete(new QueryWrapper<MallShopAdminJob>().eq("admin_id", adminId));
    }

    @Override
    public Boolean shopAdminUse(Integer jobId) {
        MallShopAdminJob job = mallShopAdminJobMapper.selectOne(new QueryWrapper<MallShopAdminJob>().eq("job_id", jobId).eq("delete_time", 0).last("limit 1"));
        return StringUtils.isNull(job) ? true : false;
    }
}
