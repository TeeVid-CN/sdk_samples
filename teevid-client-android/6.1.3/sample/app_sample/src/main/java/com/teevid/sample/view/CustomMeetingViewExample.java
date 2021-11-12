package com.teevid.sample.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teevid.sample.R;
import com.teevid.sample.view.container.VideoViewContainer;
import com.teevid.sdk.constant.CameraProvider;
import com.teevid.sdk.data.ParticipantInfo;
import com.teevid.sdk.view.BaseMeetingView;
import com.teevid.sdk.view.VideoView;
import com.teevid.sdk.view.ZoomLayout;

import java.util.ArrayList;
import java.util.List;

public class CustomMeetingViewExample extends BaseMeetingView {

    private static final String TAG = "CustomMeetingViewExampl";

    private final VideoView viewPictureInPicture;
    private final ZoomLayout layoutFullscreen;
    private final VideoView viewFullscreen;
    private final LinearLayout layoutLinear;

    private final DragTouchListener dragTouchListener;

    private final VideoViewContainer<VideoView> viewGrid;
    private final VideoViewContainer<VideoView> viewList;
    private int maxViewsInGrid = 4;

    private int lastPictureViewVisibility;

    public CustomMeetingViewExample(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_meeting_custom, this, true);

        viewPictureInPicture = findViewById(R.id.view_picture_in_picture);
        layoutFullscreen = findViewById(R.id.layout_fullscreen);
        viewFullscreen = findViewById(R.id.view_fullscreen);
        viewGrid = findViewById(R.id.view_grid);
        viewList = findViewById(R.id.view_list);
        layoutLinear = findViewById(R.id.layout_linear);

        viewPictureInPicture.setZOrderMediaOverlay(true);
        dragTouchListener = new DragTouchListener(viewPictureInPicture);
        viewPictureInPicture.setOnTouchListener(dragTouchListener);
        makeLocalVideoFullscreen();

        setKeepScreenOn(true);

// Reserved for testing purposes
//        Random random = new Random();
//        int minValue = 64;
//        int maxValue = 128;
//        viewPictureInPicture.setOnClickListener(v -> {
//
//            long id = System.currentTimeMillis();
//            String participantId = "" + id;
//
//            int color = Color.argb(255, minValue + random.nextInt(maxValue),
//                    minValue + random.nextInt(maxValue),
//                    minValue + random.nextInt(maxValue));
//
//            VideoView view = new VideoView(getContext());
//            view.setTag(id);
//            log.d(TAG, "onClick: create " + view);
//
//            view.setOnClickListener(view1 -> {
//                log.d(TAG, "onClick: remove " + view);
//                getRemoteVideoViews().remove(view);
//                onRemoveVideoView(view, null);
//            });
//
//            view.setBackgroundColor(color);
//
//            getRemoteVideoViews().add(view);
//            onAddVideoView(view, null);
//        });
    }

    @Override
    protected void onOrientationChanged(int orientation) {
        dragTouchListener.onOrientationChanged();
        layoutFullscreen.restoreDefaultState();
        viewGrid.setLayoutOrientationMode(orientation);
        viewList.setLayoutOrientationMode(orientation);

        int layoutOrientation;

        LinearLayout.LayoutParams paramsGrid = (LinearLayout.LayoutParams) viewGrid.getLayoutParams();
        paramsGrid.weight = 1;

        LinearLayout.LayoutParams paramsScreenSharing = (LinearLayout.LayoutParams)
                layoutFullscreen.getLayoutParams();

        LinearLayout.LayoutParams paramsList = (LinearLayout.LayoutParams) viewList.getLayoutParams();
        FrameLayout.LayoutParams paramsLocalVideo = (LayoutParams) viewPictureInPicture.getLayoutParams();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutOrientation = LinearLayout.VERTICAL;

            paramsGrid.width = ViewGroup.LayoutParams.MATCH_PARENT;
            paramsGrid.height = 0;

            paramsScreenSharing.width = ViewGroup.LayoutParams.MATCH_PARENT;
            paramsScreenSharing.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            paramsList.width = ViewGroup.LayoutParams.MATCH_PARENT;
            paramsList.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            if (paramsLocalVideo.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                paramsLocalVideo.width = (int) getResources()
                        .getDimension(R.dimen.local_video_small_width);
                paramsLocalVideo.height = (int) getResources()
                        .getDimension(R.dimen.local_video_small_height);
            }
        } else {
            layoutOrientation = LinearLayout.HORIZONTAL;

            paramsGrid.width = 0;
            paramsGrid.height = ViewGroup.LayoutParams.MATCH_PARENT;

            paramsScreenSharing.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            paramsScreenSharing.height = ViewGroup.LayoutParams.MATCH_PARENT;

            paramsList.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            paramsList.height = ViewGroup.LayoutParams.MATCH_PARENT;

            if (paramsLocalVideo.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                paramsLocalVideo.width = (int) getResources()
                        .getDimension(R.dimen.local_video_small_height);
                paramsLocalVideo.height = (int) getResources()
                        .getDimension(R.dimen.local_video_small_width);
            }
        }

        layoutLinear.setOrientation(layoutOrientation);
        viewGrid.setLayoutParams(paramsGrid);
        layoutFullscreen.setLayoutParams(paramsScreenSharing);
        viewList.setLayoutParams(paramsList);
        viewPictureInPicture.setLayoutParams(paramsLocalVideo);
    }

    @Override
    protected VideoView getLocalVideoView() {
        return viewPictureInPicture;
    }

    @Override
    protected VideoView getScreenSharingVideoView(ParticipantInfo participantInfo) {
        Log.d(TAG, "getScreenSharingVideoView: " + participantInfo);
        layoutFullscreen.setZoomEnabled(true);
        layoutFullscreen.setVisibility(View.VISIBLE);
        viewPictureInPicture.setVisibility(View.GONE);
        viewGrid.setVisibility(View.GONE);
        moveAllViewsToList();
        return viewFullscreen;
    }

    @Override
    protected void onRemoveScreenSharingVideoView(ParticipantInfo participantInfo) {
        Log.d(TAG, "onRemoveScreenSharingVideoView: " + participantInfo);
        layoutFullscreen.setZoomEnabled(false);
        layoutFullscreen.setVisibility(View.GONE);
        viewPictureInPicture.setVisibility(View.VISIBLE);
        viewGrid.setVisibility(View.VISIBLE);
        moveViewsFromListToGridIfPossible();
    }

    @Override
    protected VideoView getStoryboardVideoView() {
        Log.d(TAG, "getStoryboardVideoView");
        layoutFullscreen.setVisibility(View.VISIBLE);
        viewFullscreen.setZOrderMediaOverlay(true);
        lastPictureViewVisibility = viewPictureInPicture.getVisibility();
        viewPictureInPicture.setVisibility(View.GONE);
        viewGrid.setVisibility(View.GONE);
        viewList.setVisibility(View.GONE);
        return viewFullscreen;
    }

    @Override
    protected void onRemoveStoryboardVideoView() {
        Log.d(TAG, "onRemoveStoryboardVideoView");
        layoutFullscreen.setVisibility(View.GONE);
        viewFullscreen.setZOrderMediaOverlay(false);
        viewPictureInPicture.setVisibility(lastPictureViewVisibility);
        viewGrid.setVisibility(View.VISIBLE);
        viewList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onAddVideoView(VideoView view, ParticipantInfo participantInfo) {
        Log.d(TAG, "onAddVideoView: " + participantInfo + ", " + view);
        int count = viewGrid.itemCount() + viewList.itemCount() + 1;
        Log.d(TAG, "onAddVideoView: count " + count);

        if (getMode() == MODE_DEFAULT) {
            if (count <= maxViewsInGrid) {
                addViewToGridViewContainer(view);

                if (count == 1) {
                    makeLocalVideoSmall();
                }
            } else {
                addViewToListViewContainer(view);
            }
        } else { // Screen sharing is on, all views should go to list
            addViewToListViewContainer(view);
        }
    }

    @Override
    protected void onRemoveVideoView(VideoView view, ParticipantInfo participantInfo) {
        Log.d(TAG, "onRemoveVideoView: " + participantInfo + ", " + view);

        int count = getRemoteVideoViews().size();

        if (viewList.contains(view)) {
            Log.d(TAG, "onRemoveVideoView: " + "remove from list");
            viewList.removeView(view);
        } else {
            Log.d(TAG, "onRemoveVideoView: " + "remove from grid");
            viewGrid.removeView(view);

            // If there's any view in list, move the first one to grid
            boolean canMoveToGrid = !viewList.isEmpty() && isInDefaultMeetingMode();
            if (canMoveToGrid) {
                VideoView listChildView = viewList.get(0);
                viewList.removeView(listChildView);

                addViewToGridViewContainer(listChildView);
            }
        }
        boolean needToMakeFullscreen = count == 0 && isInDefaultMeetingMode();
        if (needToMakeFullscreen) {
            makeLocalVideoFullscreen();
        }
    }

    @Override
    protected void onShowLocalVideo() {
        if (isInDefaultMeetingMode()) {
            viewPictureInPicture.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onHideLocalVideo() {
        viewPictureInPicture.setVisibility(View.GONE);
    }

    @Override
    protected void onSetMaxViewsInGrid(int maxViewsInGrid) {
        this.maxViewsInGrid = maxViewsInGrid;
    }

    @Override
    protected boolean allowVideoViewReordering() {
        // Called to clarify whether a video view reordering is allowed. Video view reordering is
        // usually caused by the change of active speaker.
        return true;
    }

    @Override
    protected void onLocalVideoCameraChanged(@CameraProvider int cameraProvider) {
        Log.d(TAG, "onLocalVideoCameraChanged: " + cameraProvider);
        if (CameraProvider.FRONT_FACING == cameraProvider) {
            viewPictureInPicture.setMirror(true);
        } else {
            viewPictureInPicture.setMirror(false);
        }
    }

    private void addViewToGridViewContainer(VideoView view) {
        Log.d(TAG, "addViewToGridViewContainer: " + view);
        viewGrid.addView(view);
    }

    private void addViewToListViewContainer(VideoView view) {
        Log.d(TAG, "addViewToGridViewContainer: " + view);
        viewList.addView(view);
    }

    private void moveAllViewsToList() {
        Log.d(TAG, "moveAllViewsToList");
        List<VideoView> gridViews = new ArrayList<>(viewGrid.getVideoViews());
        viewGrid.clear();

        handler.post(() -> {
            for (VideoView gridVideoView : gridViews) {
                onAddVideoView(gridVideoView, null);
            }
        });
    }

    private void moveViewsFromListToGridIfPossible() {
        Log.d(TAG, "moveViewsFromListToGridIfPossible");
        List<VideoView> listViews = new ArrayList<>(viewList.getVideoViews());
        viewList.clear();

        for (VideoView listVideoView : listViews) {
            onAddVideoView(listVideoView, null);
        }

        List<VideoView> videoViews = getRemoteVideoViews();
        if (videoViews.isEmpty() && getMode() != MODE_SCREEN_SHARE) {
            // A situation when screen sharing view is removed along with the last video view
            makeLocalVideoFullscreen();
        }
    }

    private void makeLocalVideoSmall() {
        int width;
        int height;
        if (Configuration.ORIENTATION_PORTRAIT == getOrientation()) {
            width = (int) getResources().getDimension(R.dimen.local_video_small_width);
            height = (int) getResources().getDimension(R.dimen.local_video_small_height);
        } else {
            width = (int) getResources().getDimension(R.dimen.local_video_small_height);
            height = (int) getResources().getDimension(R.dimen.local_video_small_width);
        }
        setLocalVideoSize(width, height);

        dragTouchListener.enable();
        dragTouchListener.moveViewToDesiredPosition();
    }

    private void makeLocalVideoFullscreen() {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLocalVideoSize(width, height);

        dragTouchListener.disable();
    }

    private void setLocalVideoSize(int width, int height) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        viewPictureInPicture.setLayoutParams(params);
    }

    private boolean isInDefaultMeetingMode() {
        int mode = getMode();
        return mode != MODE_SCREEN_SHARE
                && mode != MODE_STORYBOARD;
    }
}
