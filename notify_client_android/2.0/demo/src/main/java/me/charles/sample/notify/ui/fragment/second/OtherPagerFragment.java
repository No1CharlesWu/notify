package me.charles.sample.notify.ui.fragment.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.charles.fragmentation.SupportFragment;
import me.charles.sample.R;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class OtherPagerFragment extends SupportFragment {

    public static OtherPagerFragment newInstance() {

        Bundle args = new Bundle();
        OtherPagerFragment fragment = new OtherPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_second_pager_other, container, false);
        return view;
    }
}
