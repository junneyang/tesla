package io.github.tesla.ops.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.tesla.ops.system.dao.DeptDao;
import io.github.tesla.ops.system.dao.UserDao;
import io.github.tesla.ops.system.dao.UserRoleDao;
import io.github.tesla.ops.system.domain.DeptDO;
import io.github.tesla.ops.system.domain.Tree;
import io.github.tesla.ops.system.domain.UserDO;
import io.github.tesla.ops.system.domain.UserRoleDO;
import io.github.tesla.ops.system.service.UserService;
import io.github.tesla.ops.utils.BuildTree;

@Transactional
@Service
public class UserServiceImpl implements UserService {
  @Autowired
  UserDao userMapper;
  @Autowired
  UserRoleDao userRoleMapper;
  @Autowired
  DeptDao deptMapper;

  @Override
  public UserDO get(Long id) {
    List<Long> roleIds = userRoleMapper.listRoleId(id);
    UserDO user = userMapper.get(id);
    user.setDeptName(deptMapper.get(user.getDeptId()).getName());
    user.setroleIds(roleIds);
    return user;
  }

  @Override
  public List<UserDO> list(Map<String, Object> map) {
    return userMapper.list(map);
  }

  @Override
  public int count(Map<String, Object> map) {
    return userMapper.count(map);
  }

  @Transactional
  @Override
  public int save(UserDO user) {
    int count = userMapper.save(user);
    Long userId = user.getUserId();
    List<Long> roles = user.getroleIds();
    userRoleMapper.removeByUserId(userId);
    List<UserRoleDO> list = new ArrayList<>();
    for (Long roleId : roles) {
      UserRoleDO ur = new UserRoleDO();
      ur.setUserId(userId);
      ur.setRoleId(roleId);
      list.add(ur);
    }
    if (list.size() > 0) {
      userRoleMapper.batchSave(list);
    }
    return count;
  }

  @Override
  public int update(UserDO user) {
    int r = userMapper.update(user);
    Long userId = user.getUserId();
    List<Long> roles = user.getroleIds();
    userRoleMapper.removeByUserId(userId);
    List<UserRoleDO> list = new ArrayList<>();
    for (Long roleId : roles) {
      UserRoleDO ur = new UserRoleDO();
      ur.setUserId(userId);
      ur.setRoleId(roleId);
      list.add(ur);
    }
    if (list.size() > 0) {
      userRoleMapper.batchSave(list);
    }
    return r;
  }

  @Override
  public int remove(Long userId) {
    userRoleMapper.removeByUserId(userId);
    return userMapper.remove(userId);
  }

  @Override
  public boolean exit(Map<String, Object> params) {
    boolean exit;
    exit = userMapper.list(params).size() > 0;
    return exit;
  }

  @Override
  public Set<String> listRoles(Long userId) {
    return null;
  }

  @Override
  public int resetPwd(UserDO user) {
    int r = userMapper.update(user);
    return r;
  }

  @Transactional
  @Override
  public int batchremove(Long[] userIds) {
    int count = userMapper.batchRemove(userIds);
    userRoleMapper.batchRemoveByUserId(userIds);
    return count;
  }

  @Override
  public Tree<DeptDO> getTree() {
    List<Tree<DeptDO>> trees = new ArrayList<Tree<DeptDO>>();
    List<DeptDO> depts = deptMapper.list(new HashMap<String, Object>(16));
    Long[] pDepts = deptMapper.listParentDept();
    Long[] uDepts = userMapper.listAllDept();
    Long[] allDepts = (Long[]) ArrayUtils.addAll(pDepts, uDepts);
    for (DeptDO dept : depts) {
      if (!ArrayUtils.contains(allDepts, dept.getDeptId())) {
        continue;
      }
      Tree<DeptDO> tree = new Tree<DeptDO>();
      tree.setId(dept.getDeptId().toString());
      tree.setParentId(dept.getParentId().toString());
      tree.setText(dept.getName());
      Map<String, Object> state = new HashMap<>(16);
      state.put("opened", true);
      state.put("mType", "dept");
      tree.setState(state);
      trees.add(tree);
    }
    List<UserDO> users = userMapper.list(new HashMap<String, Object>(16));
    for (UserDO user : users) {
      Tree<DeptDO> tree = new Tree<DeptDO>();
      tree.setId(user.getUserId().toString());
      tree.setParentId(user.getDeptId().toString());
      tree.setText(user.getName());
      Map<String, Object> state = new HashMap<>(16);
      state.put("opened", true);
      state.put("mType", "user");
      tree.setState(state);
      trees.add(tree);
    }
    Tree<DeptDO> t = BuildTree.build(trees);
    return t;
  }

}
