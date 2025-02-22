package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import android.graphics.drawable.GradientDrawable;

public class PaginationController {

    // 페이지 버튼 클릭 시 호출할 리스너 인터페이스
    public interface OnPageSelectedListener {
        void onPageSelected(int page);
    }

    private Context context;
    private LinearLayout container;
    private int currentPage;
    private int totalPages;
    private int pagesPerGroup;
    private OnPageSelectedListener listener;

    /**
     * PaginationController 생성자
     *
     * @param context       Context
     * @param container     페이지 버튼들을 추가할 LinearLayout 컨테이너
     * @param currentPage   현재 페이지 번호
     * @param totalPages    총 페이지 수
     * @param pagesPerGroup 한 그룹당 표시할 페이지 번호 수 (예: 5)
     * @param listener      페이지 버튼 클릭 시 호출할 리스너
     */
    public PaginationController(Context context, LinearLayout container,
                                int currentPage, int totalPages, int pagesPerGroup,
                                OnPageSelectedListener listener) {
        this.context = context;
        this.container = container;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pagesPerGroup = pagesPerGroup;
        this.listener = listener;
    }

    /**
     * 페이지 네비게이션 버튼들을 컨테이너에 XML로 인플레이트하여 동적으로 생성하고 표시합니다.
     */

    public void render() {
        // 기존 버튼 제거
        container.removeAllViews();
        // LayoutInflater 생성
        LayoutInflater inflater = LayoutInflater.from(context);

        // 현재 페이지 그룹 계산 (예: 1~5, 6~10 등)
        int groupStart = ((currentPage - 1) / pagesPerGroup) * pagesPerGroup + 1;
        int groupEnd = Math.min(groupStart + pagesPerGroup - 1, totalPages);

        // 이전 그룹 버튼 (Prev)
        if (groupStart > 1) {
            Button prevButton = (Button) inflater.inflate(R.layout.pagination_button, container, false);
            prevButton.setText("◁");
            prevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = groupStart - 1;
                    if (listener != null) {
                        listener.onPageSelected(currentPage);
                    }
                    render();
                }
            });
            container.addView(prevButton);
        }

        // 현재 그룹의 페이지 번호 버튼 생성
        for (int i = groupStart; i <= groupEnd; i++) {
            Button pageButton = (Button) inflater.inflate(R.layout.pagination_button, container, false);
            pageButton.setText(String.valueOf(i));
            final int pageNum = i;
            pageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = pageNum;
                    if (listener != null) {
                        listener.onPageSelected(pageNum);
                    }
                    render();
                }
            });

            // 현재 페이지 강조: 테두리와 텍스트 색상 변경
            if (i == currentPage) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                // 내부 채우기 색상: 투명
                drawable.setColor(ContextCompat.getColor(context, android.R.color.transparent));
                int strokeWidth = 4; // 픽셀 단위
                int strokeColor = ContextCompat.getColor(context, android.R.color.holo_blue_light);
                drawable.setStroke(strokeWidth, strokeColor);
                // 선택된 버튼에 변경된 배경과 텍스트 색상 적용
                pageButton.setBackground(drawable);
                pageButton.setTextColor(strokeColor);
            }
            container.addView(pageButton);
        }

        // 다음 그룹 버튼 (Next)
        if (groupEnd < totalPages) {
            Button nextButton = (Button) inflater.inflate(R.layout.pagination_button, container, false);
            nextButton.setText("▷");
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = groupEnd + 1;
                    if (listener != null) {
                        listener.onPageSelected(currentPage);
                    }
                    render();
                }
            });
            container.addView(nextButton);
        }
    }


    public void setCurrentPage ( int currentPage){
        this.currentPage = currentPage;
    }

    public int getCurrentPage () {
        return currentPage;
    }
}
