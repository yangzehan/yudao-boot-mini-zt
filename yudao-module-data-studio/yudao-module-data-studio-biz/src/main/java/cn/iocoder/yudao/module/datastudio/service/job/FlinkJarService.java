package cn.iocoder.yudao.module.datastudio.service.job;

import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;

/**
 * @author yzh
 */
public interface FlinkJarService {

  String deploy(JobDeployReqVO reqDto);
}
