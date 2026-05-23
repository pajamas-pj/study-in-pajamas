package com.pajamas.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pajamas.api.cache.CategoryCache;
import com.pajamas.api.client.course.CatalogueClient;
import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.client.search.SearchClient;
import com.pajamas.api.client.user.UserClient;
import com.pajamas.api.dto.course.CataSimpleInfoDTO;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.api.dto.user.UserDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.StringUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.learning.domain.dto.QuestionFormDTO;
import com.pajamas.learning.domain.po.InteractionQuestion;
import com.pajamas.learning.domain.po.InteractionReply;
import com.pajamas.learning.domain.query.QuestionAdminPageQuery;
import com.pajamas.learning.domain.query.QuestionPageQuery;
import com.pajamas.learning.domain.vo.QuestionAdminVO;
import com.pajamas.learning.domain.vo.QuestionVO;
import com.pajamas.learning.mapper.InteractionQuestionMapper;
import com.pajamas.learning.service.IInteractionQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-26
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {
    private final IInteractionReplyService replyService;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final SearchClient searchClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;
    @Override
    public void saveQuestion(QuestionFormDTO formDTO) {
        //1.获取登录用户
        Long userId = UserContext.getUser();
        //2.数据转换
        InteractionQuestion question = BeanUtils.toBean(formDTO, InteractionQuestion.class);
        //3.数据填充
        question.setId(userId);
        //4.保存数据
        save(question);
    }

    @Override
    public void changeQuestion(Long questionId, QuestionFormDTO formDTO) {
        //1.查询问题表单
        InteractionQuestion question = getById(questionId);
        //2.修改问题表单
        question.setTitle(formDTO.getTitle());
        question.setDescription(formDTO.getDescription());
        question.setAnonymity(formDTO.getAnonymity());
        //3.保存数据
        updateById(question);
    }

    @Override
    public void deleteQuestion(Long questionId, QuestionFormDTO formDTO) {
        //1.查询问题表单
        InteractionQuestion question = getById(questionId);
        //2.删除问题表单
        removeById(questionId);
    }
    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        //1.参数校验，课程id和小节id不能同时为空
        Long courseId = query.getCourseId();
        Long sectionId = query.getSectionId();
        if (courseId == null && sectionId == null) {
            throw new BadRequestException("课程id和小节id不能同时为空");
        }
        //2.分页查询
        Page<InteractionQuestion> page = lambdaQuery()
                .select(InteractionQuestion.class, info -> !info.getProperty().equals("description"))
                //select选择不返回字段
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, UserContext.getUser())
                .eq(courseId != null, InteractionQuestion::getCourseId, courseId)
                .eq(sectionId != null, InteractionQuestion::getSectionId, sectionId)
                .eq(InteractionQuestion::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //3.根据id查询提问者信息和最近一次回答信息
        //3.1.根据id查询提问者id和最近一次回答者id
        HashSet<Long> userIds = new HashSet<>();
        HashSet<Long> answerUserIds = new HashSet<>();
        for(InteractionQuestion q : records){
            if(!q.getAnonymity()){//只查询非匿名用户
                userIds.add(q.getUserId());
            }
            answerUserIds.add(q.getLatestAnswerId());
        }
        //3.2.根据id查询最近一次回答信息
        answerUserIds.remove(null);
        Map<Long, InteractionReply> replyMap = new HashMap<>(answerUserIds.size());
        if(!CollUtils.isEmpty(answerUserIds)){
            List<InteractionReply> replies = replyService.listByIds(answerUserIds);
            for(InteractionReply r : replies){
                replyMap.put(r.getId(), r);
                if(!r.getAnonymity()){
                    userIds.add(r.getUserId());//添加最近一次回答者id
                }
            }
        }
        //3.3.根据id查询提问者信息
        userIds.remove(null);
        Map<Long, UserDTO> userMap = new HashMap<>(userIds.size());
        if(!CollUtils.isEmpty(answerUserIds)){
            List<UserDTO> users = userClient.queryUserByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        }
        //4.封装
        List<QuestionVO> list = new ArrayList<>(records.size());
        for(InteractionQuestion q : records){
            //4.1.封装VO
            QuestionVO vo = BeanUtils.toBean(q, QuestionVO.class);
            //4.2.设置提问者信息
            UserDTO userDTO = userMap.get(q.getUserId());
            if(!q.getAnonymity()){
                if(userDTO != null){
                    vo.setUserName(userDTO.getName());
                    vo.setUserIcon(userDTO.getIcon());
                }
            }
            //4.3.设置最近一次回答信息
            InteractionReply reply = replyMap.get(q.getLatestAnswerId());
            if(reply != null){
                vo.setLatestReplyContent(reply.getContent());
                if(!reply.getAnonymity()){
                    UserDTO replyUser = userMap.get(reply.getUserId());
                    vo.setLatestReplyUser(replyUser.getName());
                }
            }
        }
        return PageDTO.of(page, list);
    }
    @Override
    public QuestionVO queryQuestionById(Long id) {
        //1.查询问题表单
        InteractionQuestion question = getById(id);
        //2.校验
        if(question == null){
            return null;
        }
        //3.查提问者
        UserDTO user = null;
        if(!question.getAnonymity()){
            user = userClient.queryUserById(question.getUserId());
        }
        //4.封装VO
        QuestionVO vo = BeanUtils.toBean(question, QuestionVO.class);
        if(user != null){
            vo.setUserName(user.getName());
            vo.setUserIcon(user.getIcon());
        }
        return vo;
    }
    @Override
    public PageDTO<QuestionAdminVO> queryQuestionAdminPage(QuestionAdminPageQuery query) {
        //1.处理课程名称，查询课程id
        List<Long> courseIds = null;
        if(StringUtils.isNotBlank(query.getCourseName())){
            //模糊搜索返回集合
            courseIds = searchClient.queryCoursesIdByName(query.getCourseName());
            if(CollUtils.isEmpty(courseIds)){
                return PageDTO.empty(0L,0l);
            }
        }
        //2.分页查询
        Integer status = query.getStatus();
        LocalDateTime begin = query.getBeginTime();
        LocalDateTime end = query.getEndTime();
        Page<InteractionQuestion> page = lambdaQuery()
                .in(courseIds != null, InteractionQuestion::getCourseId, courseIds)
                .eq(status != null, InteractionQuestion::getStatus, status)
                .gt(begin != null, InteractionQuestion::getCreateTime, begin)
                .lt(end != null, InteractionQuestion::getCreateTime, end)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //3.准备用户数据，课程数据，章节数据，分类数据
        Set<Long> userIds = new HashSet<>();
        Set<Long> cIds = new HashSet<>();
        Set<Long> cataIds = new HashSet<>();

        for(InteractionQuestion q : records){
            userIds.add(q.getUserId());
            cIds.add(q.getCourseId());
            cataIds.add(q.getChapterId());
            cataIds.add(q.getSectionId());
        }
        // 3.2.根据id查询用户
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = new HashMap<>(users.size());
        if (CollUtils.isNotEmpty(users)) {
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        }

        // 3.3.根据id查询课程
        List<CourseSimpleInfoDTO> cInfos = courseClient.getSimpleInfoList(cIds);
        Map<Long, CourseSimpleInfoDTO> cInfoMap = new HashMap<>(cInfos.size());
        if (CollUtils.isNotEmpty(cInfos)) {
            cInfoMap = cInfos.stream()
                    .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        }

        // 3.4.根据id查询章节
        List<CataSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(cataIds);
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(CataSimpleInfoDTO::getId,
                            CataSimpleInfoDTO::getName));
        }

        //4.封装VO
        List<QuestionAdminVO> voList = new ArrayList<>(records.size());
        for(InteractionQuestion q : records){
            // 4.1.将PO转VO，属性拷贝
            QuestionAdminVO vo = BeanUtils.copyBean(q, QuestionAdminVO.class);
            voList.add(vo);
            // 4.2.用户信息
            UserDTO user = userMap.get(q.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
            }
            // 4.3.课程信息以及分类信息
            CourseSimpleInfoDTO cInfo = cInfoMap.get(q.getCourseId());
            if (cInfo != null) {
                vo.setCourseName(cInfo.getName());
                vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            }
            // 4.4.章节信息
            vo.setChapterName(cataMap.getOrDefault(q.getChapterId(), ""));
            vo.setSectionName(cataMap.getOrDefault(q.getSectionId(), ""));
        }
        return PageDTO.of(page, voList);
    }
}
