package com.Backend.shareNote.domain.Oraganization.entity;

import com.Backend.shareNote.domain.Oraganization.exception.SelfLikedException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "organizations")
@Builder
@Getter
public class Organization {

    @Id
    private String id;

    private String name;
    @Setter
    private String description;

    private String owner; //여기 email 들어가는거야
    private List<String> members;

    private List<String> quiz;

    private List<Note> notes;

    private String emoji;
    @CreatedDate
    private LocalDateTime createdAt;

    // 내부 클래스로 Note 정의
    @Builder //신기하다
    @Getter
    @Setter
    @Document(collection = "notes")
    public static class Note {
        @Id
        private String id;
        private String createUser;

        private String title;

        private String noteImageUrl;

        private List<Page> pages;
        //page에 order 필드 추가!!

        // 좋아요 받은 사람들
        private LikesInfo likesInfo;

        @CreatedDate
        private LocalDateTime createdAt;


        private List<Quiz> quiz;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }


    // 내부 클래스로 Page 정의
    @Getter
    @Builder
    @Document(collection = "pages")
    public static class Page {
        @Id //수동으로 id 생성
        private String id;
        private String createUser;
        @CreatedDate
        private LocalDateTime createdAt;

    }

    @Getter
    @Builder
    @Document(collection = "quizs")
    public static class Quiz {
        @Id
        private String id;
        // 생성자
        private String userId;
        // 객관식 or 주관식
        private String quizType;
        // 문제 설명
        private String problem;
        // 답안
        private int answer;
        // 객관식 보기
        private List<String> solutions;
        @CreatedDate
        private LocalDateTime createdAt;

        // 정답 맞춘 유저
        private List<String> correctUser;
        // 정답 틀린 유저
        private List<String> wrongUser;

        // 닉네임
        private String nickname;

    }


    @Getter
    @Slf4j
    public static class LikesInfo {
        private Map<String, UserLike> userLikes = new HashMap<>();

        /**
         * 좋아요를 추가하거나 취소합니다.
         * @param userUuid : 좋아요를 받은 사용자의 Id
         * @param blockId : 좋아요를 받은 블록의 Id
         * @param likerUuid : 좋아요를 누른 사용자의 Id
         * @return 좋아요 상태 변경이 성공했으면 true, 이미 해당 상태였으면 false 반환
         */
        public Boolean addLike(String userUuid, String blockId, String likerUuid) {
            // 자기 블록에 좋아요 못하게 하기
            if(userUuid.equals(likerUuid)) {
                throw new SelfLikedException("자기 블록에 좋아요를 누를 수 없습니다.");
            }
            return userLikes.computeIfAbsent(userUuid, k -> new UserLike())
                    .addBlockLike(blockId, likerUuid);

        }

        // 필요한 메서드 추가...
    }
    @Getter
    public static class UserLike {
        private Map<String, BlockLike> blockLikes = new HashMap<>();

        public Boolean addBlockLike(String blockId, String likerUuid) {
            return blockLikes.computeIfAbsent(blockId, k -> new BlockLike())
                    .addLiker(likerUuid);
        }

        // 필요한 메서드 추가...
    }
    @Getter
    public static class BlockLike {
        private List<String> likers = new ArrayList<>();

        public Boolean addLiker(String likerUuid) {
            // 좋아요 취소 로직 구현 및 좋아요 여부 확인
            if(likers.contains(likerUuid)){
                likers.remove(likerUuid);
                return false;
            }
            else{
                likers.add(likerUuid);
                return true;
            }

        }

        // 필요한 메서드 추가...
    }



    public void addPageToNote(String noteId, Page newPage) {
        for (Note note : this.notes) {
            if (note.getId().equals(noteId)) {
                note.getPages().add(newPage);
                break;
            }
        }
    }

    public void deletePageFromNote(String noteId, String pageId) {
        boolean isDeleted = false;
        // 페이지 삭제
        for (Note note : this.notes) {
            if (note.getId().equals(noteId)) {
                for (Page page : note.getPages()) {
                    if (page.getId().equals(pageId)) {
                        note.getPages().remove(page);
                        isDeleted = true;
                        break;
                    }
                }
                break;
            }
        }
        if (!isDeleted) {
            // 이게 무슨 예왼지는 모르겠지만
            throw new IllegalArgumentException("해당하는 페이지가 없습니다.");
        }
    }




}
