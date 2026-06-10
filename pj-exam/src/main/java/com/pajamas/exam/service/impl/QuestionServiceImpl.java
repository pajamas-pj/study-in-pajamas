package com.pajamas.exam.service.impl;

import com.pajamas.api.dto.exam.ExamQuestionDTO;
import com.pajamas.api.dto.exam.ExamQuestionOptionDTO;
import com.pajamas.exam.domain.dto.QuestionOptionDTO;
import com.pajamas.exam.domain.dto.QuestionSaveDTO;
import com.pajamas.exam.domain.po.Question;
import com.pajamas.exam.domain.po.QuestionDetail;
import com.pajamas.exam.domain.po.QuestionOption;
import com.pajamas.exam.domain.vo.QuestionVO;
import com.pajamas.exam.mapper.QuestionMapper;
import com.pajamas.exam.service.IQuestionDetailService;
import com.pajamas.exam.service.IQuestionOptionService;
import com.pajamas.exam.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private final IQuestionDetailService questionDetailService;
    private final IQuestionOptionService questionOptionService;

    @Override
    @Transactional
    public void saveQuestion(QuestionSaveDTO dto) {
        Question question = BeanUtils.copyBean(dto, Question.class);
        save(question);

        Long questionId = question.getId();

        saveDetail(questionId, dto.getAnswer(), dto.getExplanation());
        saveOptions(questionId, dto.getOptions());
    }

    @Override
    @Transactional
    public void updateQuestion(QuestionSaveDTO dto) {
        Long questionId = dto.getId();
        if (questionId == null) {
            throw new BadRequestException("题目ID不能为空");
        }
        Question question = getById(questionId);
        if (question == null) {
            throw new BadRequestException("题目不存在");
        }

        question.setType(dto.getType());
        question.setContent(dto.getContent());
        question.setScore(dto.getScore());
        question.setDifficulty(dto.getDifficulty());
        updateById(question);

        questionDetailService.removeById(questionId);
        saveDetail(questionId, dto.getAnswer(), dto.getExplanation());

        questionOptionService.deleteByQuestionId(questionId);
        saveOptions(questionId, dto.getOptions());
    }

    @Override
    public QuestionVO getQuestionVO(Long id) {
        Question question = getById(id);
        if (question == null) {
            throw new BadRequestException("题目不存在");
        }
        QuestionVO vo = BeanUtils.copyBean(question, QuestionVO.class);

        QuestionDetail detail = questionDetailService.getById(id);
        if (detail != null) {
            vo.setAnswer(detail.getAnswer());
            vo.setExplanation(detail.getExplanation());
        }

        List<QuestionOption> options = questionOptionService.queryByQuestionId(id);
        if (options != null && !options.isEmpty()) {
            vo.setOptions(options.stream().map(o ->
                    BeanUtils.copyBean(o, QuestionOptionDTO.class)
            ).collect(Collectors.toList()));
        }

        return vo;
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (getById(id) == null) {
            throw new BadRequestException("题目不存在");
        }
        questionDetailService.removeById(id);
        questionOptionService.deleteByQuestionId(id);
        removeById(id);
    }

    @Override
    public List<ExamQuestionDTO> queryQuestionsByIds(List<Long> ids) {
        //1. 参数校验
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        //2. 批量查询题目主表
        List<Question> questions = lambdaQuery().in(Question::getId, ids).list();
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        //3. 批量查询选项，按题目ID分组
        List<QuestionOption> options = questionOptionService.lambdaQuery()
                .in(QuestionOption::getQuestionId, (Collection<Long>) ids)
                .orderByAsc(QuestionOption::getSortOrder)
                .list();
        Map<Long, List<QuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        //4. 批量查询题目详情（答案和解析）
        List<QuestionDetail> details = questionDetailService.listByIds(ids);
        Map<Long, QuestionDetail> detailMap = details.stream()
                .collect(Collectors.toMap(QuestionDetail::getQuestionId, d -> d, (a, b) -> a));
        //5. 内存组装 VO
        return questions.stream().map(q -> {
            ExamQuestionDTO vo = BeanUtils.copyBean(q, ExamQuestionDTO.class);
            QuestionDetail detail = detailMap.get(q.getId());
            if (detail != null) {
                vo.setAnswer(detail.getAnswer());
                vo.setExplanation(detail.getExplanation());
            }
            List<QuestionOption> opts = optionMap.get(q.getId());
            if (opts != null && !opts.isEmpty()) {
                vo.setOptions(opts.stream().map(o ->
                        BeanUtils.copyBean(o, ExamQuestionOptionDTO.class)
                ).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private void saveDetail(Long questionId, String answer, String explanation) {
        QuestionDetail detail = new QuestionDetail();
        detail.setQuestionId(questionId);
        detail.setAnswer(answer);
        detail.setExplanation(explanation);
        questionDetailService.save(detail);
    }

    private void saveOptions(Long questionId, List<QuestionOptionDTO> optionDTOs) {
        if (optionDTOs == null || optionDTOs.isEmpty()) {
            return;
        }
        List<QuestionOption> options = optionDTOs.stream().map(dto -> {
            QuestionOption po = BeanUtils.copyBean(dto, QuestionOption.class);
            po.setQuestionId(questionId);
            return po;
        }).collect(Collectors.toList());
        questionOptionService.saveBatch(options);
    }
}
