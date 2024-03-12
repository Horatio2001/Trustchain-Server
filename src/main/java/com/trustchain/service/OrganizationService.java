package com.trustchain.service;

import java.util.List;

import com.trustchain.enums.RegisterStatus;
import com.trustchain.model.entity.Organization;
import com.trustchain.model.entity.OrganizationRegister;
import com.trustchain.model.vo.OrganizationInformation;


public interface OrganizationService {

    /**
     * 获取机构选择列表
     *
     * @return: 机构选择列表
     */
    List<Organization> selectList();

    /**
     * 判断机构是否存在
     *
     * @param orgName: 机构名称
     * @param orgId:   机构ID
     * @return: 是否存在
     */
    boolean exist(String orgName, String orgId);

    /**
     * 机构注册申请
     *
     * @param orgReg: 机构注册对象
     * @return: 注册申请号
     */
    String registerApply(OrganizationRegister orgReg);

    /**
     * 机构注册查询
     *
     * @param regIds: 注册申请号
     * @return: 注册申请列表
     */
    List<OrganizationRegister> registerApplySearch(List<String> regIds);

    /**
     * 获取注册申请列表
     *
     * @param orgId: 机构ID
     * @return: 注册申请列表
     */
    List<OrganizationRegister> registerList(String orgId);

    /**
     * @param regId: 注册申请号
     * @return: 注册申请信息
     */
    OrganizationRegister registerDetail(String regId);

    /**
     * 注册申请回复
     *
     * @param regId:  注册申请号
     * @param reply:  回复类型
     * @param reason: 回复理由
     * @return: 成功与否
     */
    boolean registerReply(String regId, RegisterStatus reply, String reason);

    /**
     * 机构注册
     *
     * @param org: 机构
     * @return: 是否插入成功
     */
    boolean register(Organization org);

    /**
     * 获取机构详情
     *
     * @param orgId:      机构ID
     * @param version: 版本号
     * @return: 机构信息
     */
    Organization informationDetail(String orgId, String version);

    /**
     * 获取机构的下级机构列表
     *
     * @param id: 机构ID
     * @return
     */
    List<Organization> subordinateList(String orgId);

    /**
     * 获取下级机构详情
     *
     * @param orgId: 机构ID
     * @return
     */
    Organization subordinateDetail(String orgId);
}
