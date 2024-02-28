package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.constants.CategoryConstants;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.vo.CategoryTreeVo;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.model.category.Category;
import com.zbkj.common.request.CategoryRequest;
import com.zbkj.common.request.CategorySearchRequest;
import com.zbkj.service.dao.CategoryDao;
import com.zbkj.service.service.CategoryService;
import com.zbkj.service.service.SystemAttachmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CategoryServiceImpl 接口实现
*  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2022 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {

    @Resource
    private CategoryDao dao;

    @Autowired
    private SystemAttachmentService systemAttachmentService;


    /**
     * 获取分类下子类的数量
     * @param request 请求参数
     * @param pageParamRequest 分页参数
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return List<Category>
     */
    @Override
    public List<Category> getList(CategorySearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(null != request.getPid()){
            lambdaQueryWrapper.eq(Category::getPid, request.getPid());
        }
        if(null != request.getType()){
            lambdaQueryWrapper.eq(Category::getType, request.getType());
        }
        if(ObjectUtil.isNotNull(request.getStatus()) && request.getStatus() >= 0){
            lambdaQueryWrapper.eq(Category::getStatus, request.getStatus().equals(CategoryConstants.CATEGORY_STATUS_NORMAL));
        }
        if(null != request.getName()){
            lambdaQueryWrapper.like(Category::getName, request.getName());
        }
        lambdaQueryWrapper.orderByDesc(Category::getSort).orderByDesc(Category::getId);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 通过id集合获取列表
     * @param idList List<Integer> id集合
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return List<Category>
     */
    @Override
    public List<Category> getByIds(List<Integer> idList) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Category::getId, idList);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 通过id集合获取列表 id => name
     * @param cateIdList List<Integer> id集合
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return HashMap<Integer, String>
     */
    @Override
    public HashMap<Integer, String> getListInId(List<Integer> cateIdList) {
        HashMap<Integer, String> map = new HashMap<>();
        List<Category> list = getByIds(cateIdList);
        for (Category category : list){
            map.put(category.getId(), category.getName());
        }

        return map;
    }

    /**
     * 查询id和url是否存在
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return Boolean
     */
    @Override
    public Boolean checkAuth(List<Integer> pathIdList, String uri) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Category::getId, pathIdList).eq(Category::getUrl, uri);
        List<Category> categoryList = dao.selectList(lambdaQueryWrapper);
        if(categoryList.size() < 1){
            return false;
        }

        return true;
    }

    /**
     * 修改
     * @param request CategoryRequest
     * @param id Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return bool
     */
    @Override
    public boolean update(CategoryRequest request, Integer id) {
        try{
            //修改分类信息
            Category category = new Category();
            BeanUtils.copyProperties(request, category);
            category.setId(id);
            category.setPath(getPathByPId(category.getPid()));

            updateById(category);

            //如状态为关闭，那么所以子集的状态都关闭
            if(!request.getStatus()){
                updateStatusByPid(id, false);
            }else{
                //如是开启，则父类的状态为开启
                updatePidStatusById(id);
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 开启父级状态
     * @param id Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     */
    private void updatePidStatusById(Integer id) {
        Category category = getById(id);
        List<Integer> categoryIdList = CrmebUtil.stringToArrayByRegex(category.getPath(), "/");
        categoryIdList.removeIf(i -> i.equals(0));
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        if(categoryIdList.size() < 1){
            return;
        }
        for (Integer categoryId: categoryIdList) {
            Category categoryVo = new Category();
            categoryVo.setId(categoryId);
            categoryVo.setStatus(true);
            categoryArrayList.add(categoryVo);
        }
        updateBatchById(categoryArrayList);
    }

    /**
     * 获取分类下子类的数量
     * @param pid Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return bool
     */
    private int getChildCountByPid(Integer pid) {
        //查看是否有子类
        QueryWrapper<Category> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.like("path", "/"+pid+"/");
        return dao.selectCount(objectQueryWrapper);
    }

    /**
     * 修改分类以及子类的状态
     * @param pid Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return bool
     */
    private int updateStatusByPid(Integer pid, boolean status) {
        //查看是否有子类
        Category category = new Category();
        category.setStatus(status);

        QueryWrapper<Category> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.like("path", "/"+pid+"/");
        return dao.update(category, objectQueryWrapper);
    }

    private String getPathByPId(Integer pid) {
        Category category = getById(pid);
        if(null != category){
            return category.getPath() + pid + "/";
        }
        return null;
    }

    /**
     * 带结构的无线级分类
     * @author Mr.Zhang
     * @since 2020-04-16
     */
    @Override
    public List<CategoryTreeVo> getListTree(Integer type, Integer status, String name) {
        return getTree(type, status,name,null);
    }

    /**
     * 带权限的属性结构
     */
    @Override
    public List<CategoryTreeVo> getListTree(Integer type, Integer status, List<Integer> categoryIdList) {
        System.out.println("菜单列表:getListTree: type:" + type + "| status:" + status + "| categoryIdList:" + JSON.toJSONString(categoryIdList));
        return getTree(type, status,null,categoryIdList);
    }

    /**
     * 带结构的无线级分类
     * @author Mr.Zhang
     * @since 2020-04-16
     */
    private List<CategoryTreeVo> getTree(Integer type, Integer status,String name, List<Integer> categoryIdList) {
        //循环数据，把数据对象变成带list结构的vo
        List<CategoryTreeVo> treeList = new ArrayList<>();

        LambdaQueryWrapper<Category> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(Category::getType, type);

        if(null != categoryIdList && categoryIdList.size() > 0){
            lambdaQueryWrapper.in(Category::getId, categoryIdList);
        }

        if(status >= 0){
            lambdaQueryWrapper.eq(Category::getStatus, status);
        }
        if(StringUtils.isNotBlank(name)){ // 根据名称模糊搜索
            lambdaQueryWrapper.like(Category::getName,name);
        }

        lambdaQueryWrapper.orderByDesc(Category::getSort);
        lambdaQueryWrapper.orderByAsc(Category::getId);
        List<Category> allTree = dao.selectList(lambdaQueryWrapper);
        if(allTree == null){
            return null;
        }
        // 根据名称搜索特殊处理 这里仅仅处理两层搜索后有子父级关系的数据
        if(StringUtils.isNotBlank(name) && allTree.size() >0){
            List<Category> searchCategory = new ArrayList<>();
            List<Integer> categoryIds = allTree.stream().map(Category::getId).collect(Collectors.toList());

            List<Integer> pidList = allTree.stream().filter(c -> c.getPid() > 0 && !categoryIds.contains(c.getPid()))
                    .map(Category::getPid).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(pidList)) {
                pidList.forEach(pid -> {
                    searchCategory.add(dao.selectById(pid));
                });
            }
            allTree.addAll(searchCategory);
        }

        for (Category category: allTree) {
            CategoryTreeVo categoryTreeVo = new CategoryTreeVo();
            BeanUtils.copyProperties(category, categoryTreeVo);
            treeList.add(categoryTreeVo);
        }


        //返回
        Map<Integer, CategoryTreeVo> map = new HashMap<>();
        //ID 为 key 存储到map 中
        for (CategoryTreeVo categoryTreeVo1 : treeList) {
            map.put(categoryTreeVo1.getId(), categoryTreeVo1);
        }

        List<CategoryTreeVo> list = new ArrayList<>();
        for (CategoryTreeVo tree : treeList) {
            //子集ID返回对象，有则添加。
            CategoryTreeVo tree1 = map.get(tree.getPid());
            if(tree1 != null){
                tree1.getChild().add(tree);
            }else {
                list.add(tree);
            }
        }
        System.out.println("无限极分类 : getTree:" + JSON.toJSONString(list));
        return list;
    }

    /**
     * 删除分类表
     * @param id Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return bool
     */
    @Override
    public int delete(Integer id) {
        //查看是否有子类, 物理删除
        if(getChildCountByPid(id) > 0){
            throw new CrmebException("当前分类下有子类，请先删除子类！");
        }

        return dao.deleteById(id);
    }

    /**
     * 获取分类下子类
     * @param pid Integer
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return List<Category>
     */
    @Override
    public List<Category> getChildVoListByPid(Integer pid) {
        //查看是否有子类
        QueryWrapper<Category> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("status", CategoryConstants.CATEGORY_STATUS_NORMAL);
        objectQueryWrapper.like("path", "/"+pid+"/");
        return dao.selectList(objectQueryWrapper);
    }

    /**
     * 检测分类名称是否存在
     * @param name String 分类名
     * @param type int 类型
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return int
     */
    private int checkName(String name, Integer type) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getName, name);
        if (ObjectUtil.isNotNull(type)) {
            lambdaQueryWrapper.eq(Category::getType, type);
        }
        return dao.selectCount(lambdaQueryWrapper);
    }

    /**
     * 检测url是否存在
     * @param uri String url
     * @author Mr.Zhang
     * @since 2020-04-16
     * @return int
     */
    @Override
    public boolean checkUrl(String uri) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getUrl, uri);
        return dao.selectCount(lambdaQueryWrapper) > 0;
    }

    @Override
    public boolean updateStatus(Integer id) {
        Category category = getById(id);
        category.setStatus(!category.getStatus());
        return updateById(category);
    }

    /**
     * 新增分类
     * @param categoryRequest
     */
    @Override
    public Boolean create(CategoryRequest categoryRequest) {
        //检测标题是否存在
        if(checkName(categoryRequest.getName(), categoryRequest.getType()) > 0){
            throw new CrmebException("此分类已存在");
        }
        Category category = new Category();
        BeanUtils.copyProperties(categoryRequest, category);
        category.setPath(getPathByPId(category.getPid()));
        category.setExtra(systemAttachmentService.clearPrefix(category.getExtra()));
        return save(category);
    }

    /**
     * 获取文章分类列表
     * @return List<Category>
     */
    @Override
    public List<Category> findArticleCategoryList() {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.select(Category::getId, Category::getName);
        lambdaQueryWrapper.eq(Category::getType, Constants.CATEGORY_TYPE_ARTICLE);
        lambdaQueryWrapper.eq(Category::getStatus, true);
        lambdaQueryWrapper.orderByDesc(Category::getSort);
        lambdaQueryWrapper.orderByAsc(Category::getId);
        return dao.selectList(lambdaQueryWrapper);
    }
}

