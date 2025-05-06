package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;


public class BadgeFragment extends Fragment {

    public BadgeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null; // 뷰 참조 초기화
    }

    private View mView;
    private Context mContext;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mContext = requireContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_badge, container, false);

        // 제목 설정
        TextView badgeTitle = view.findViewById(R.id.badge_title);
        badgeTitle.setText("내 배지");

        // 소제목1
        TextView badgeTitlemini=view.findViewById(R.id.badge_title_mini);
        badgeTitlemini.setText("첫 러닝");

        // 소제목2
        TextView badgeTitlemini2=view.findViewById(R.id.badge_title_mini2);
        badgeTitlemini2.setText("누적 거리");

        // 소제목2
        TextView badgeTitlemini3=view.findViewById(R.id.badge_title_mini3);
        badgeTitlemini3.setText("누적 일수");

        // 소제목2
        TextView badgeTitlemini4=view.findViewById(R.id.badge_title_mini4);
        badgeTitlemini4.setText("누적 고도");


        // 배지 아이템 설정
        // 첫러닝+5경로+4주
        ImageView badgeImage1 = view.findViewById(R.id.badge_image_1);
        TextView badgeText1 = view.findViewById(R.id.badge_text_1);

        badgeImage1.setImageResource(R.drawable.bg_firstrunning);
        badgeText1.setText("첫러닝");

        ImageView badgeImage2 = view.findViewById(R.id.badge_image_2);
        TextView badgeText2 = view.findViewById(R.id.badge_text_2);

        badgeImage2.setImageResource(R.drawable.bg_5routes);
        badgeText2.setText("5가지 경로 러닝");

        ImageView badgeImage3 = view.findViewById(R.id.badge_image_3);
        TextView badgeText3 = view.findViewById(R.id.badge_text_3);

        badgeImage3.setImageResource(R.drawable.bg_4weeks);
        badgeText3.setText("4주 연속 러닝");

        // 누적 거리
        ImageView badgeImage4 = view.findViewById(R.id.badge_image_4);
        TextView badgeText4 = view.findViewById(R.id.badge_text_4);

        badgeImage4.setImageResource(R.drawable.bg_50km);
        badgeText4.setText("50km");

        ImageView badgeImage5 = view.findViewById(R.id.badge_image_5);
        TextView badgeText5 = view.findViewById(R.id.badge_text_5);

        badgeImage5.setImageResource(R.drawable.bg_100km);
        badgeText5.setText("100km");

        ImageView badgeImage6 = view.findViewById(R.id.badge_image_6);
        TextView badgeText6 = view.findViewById(R.id.badge_text_6);

        badgeImage6.setImageResource(R.drawable.bg_200km);
        badgeText6.setText("200km");

        ImageView badgeImage7 = view.findViewById(R.id.badge_image_7);
        TextView badgeText7 = view.findViewById(R.id.badge_text_7);

        badgeImage7.setImageResource(R.drawable.bg_500km);
        badgeText7.setText("500km");

        //누적 일수
        ImageView badgeImage8 = view.findViewById(R.id.badge_image_8);
        TextView badgeText8 = view.findViewById(R.id.badge_text_8);

        badgeImage8.setImageResource(R.drawable.bg_30days);
        badgeText8.setText("30일");

        ImageView badgeImage9 = view.findViewById(R.id.badge_image_9);
        TextView badgeText9 = view.findViewById(R.id.badge_text_9);

        badgeImage9.setImageResource(R.drawable.bg_50days);
        badgeText9.setText("50일");

        ImageView badgeImage10 = view.findViewById(R.id.badge_image_10);
        TextView badgeText10 = view.findViewById(R.id.badge_text_10);

        badgeImage10.setImageResource(R.drawable.bg_100days);
        badgeText10.setText("100일");

        ImageView badgeImage11 = view.findViewById(R.id.badge_image_11);
        TextView badgeText11 = view.findViewById(R.id.badge_text_11);

        badgeImage11.setImageResource(R.drawable.bg_200days);
        badgeText11.setText("200일");

        // 누적 고도
        ImageView badgeImage12 = view.findViewById(R.id.badge_image_12);
        TextView badgeText12 = view.findViewById(R.id.badge_text_12);

        badgeImage12.setImageResource(R.drawable.bg_500m);
        badgeText12.setText("500m");

        ImageView badgeImage13 = view.findViewById(R.id.badge_image_13);
        TextView badgeText13 = view.findViewById(R.id.badge_text_13);

        badgeImage13.setImageResource(R.drawable.bg_1000m);
        badgeText13.setText("1000m");

        ImageView badgeImage14 = view.findViewById(R.id.badge_image_14);
        TextView badgeText14 = view.findViewById(R.id.badge_text_14);

        badgeImage14.setImageResource(R.drawable.bg_2000m);
        badgeText14.setText("2000m");

        ImageView badgeImage15 = view.findViewById(R.id.badge_image_15);
        TextView badgeText15 = view.findViewById(R.id.badge_text_15);

        badgeImage15.setImageResource(R.drawable.bg_5000m);
        badgeText15.setText("5000m");

        return view;
    }
}