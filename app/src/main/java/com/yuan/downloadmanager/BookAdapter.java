package com.yuan.downloadmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.library.dmanager.download.BookDownloadManager;
import com.yuan.library.dmanager.download.DownloadTask;
import com.yuan.library.dmanager.download.DownloadTaskListener;
import com.yuan.library.dmanager.download.PlayChapterDescriptor;
import com.yuan.library.dmanager.download.TaskEntity;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_CANCEL;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_CONNECTING;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_DOWNLOADING;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_FINISH;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_INIT;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_PAUSE;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_QUEUE;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_REQUEST_ERROR;
import static com.yuan.library.dmanager.download.TaskStatus.TASK_STATUS_STORAGE_ERROR;

/**
 * Created by Yuan on 9/19/16:2:31 PM.
 * <p/>
 * Description:com.yuan.downloadmanager.ListAdapter
 */

class BookAdapter extends RecyclerView.Adapter<BookAdapter.CViewHolder> {

    private Context mContext;

    private List<TestEntity> mListData;


    BookAdapter(Context context, List<TestEntity> list) {
        mContext = context;
        mListData = list;
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CViewHolder holder, final int position) {

        final TestEntity entity = mListData.get(holder.getAdapterPosition());
        holder.titleView.setText(entity.getTitle());

        if (TextUtils.isEmpty(entity.getUrl())) {
            entity.setUrl("the item of " + holder.getAdapterPosition() + " is empty url...");
        }
        holder.itemView.setTag(entity.getUrl());
        String taskId = String.valueOf(position);
        final PlayChapterDescriptor item = new PlayChapterDescriptor();
        item.chapterId = taskId;
        item.bookId = taskId;
        DownloadTask itemTask = BookDownloadManager.getInstance().getDownloadTask(item);


        if (itemTask == null) {
            holder.downloadButton.setText(R.string.start);
            holder.progressView.setText("0");
            holder.progressBar.setProgress(0);
        } else {
            TaskEntity taskEntity = itemTask.getTaskEntity();
            int status = taskEntity.getTaskStatus();
            responseUIListener(itemTask, holder);
            String progress = getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize());
            switch (status) {
                case TASK_STATUS_INIT:
                    boolean isPause = BookDownloadManager.getInstance().isPauseTask(taskEntity.getTaskId());
                    boolean isFinish = BookDownloadManager.getInstance().isFinishTask(taskEntity.getTaskId());
                    holder.downloadButton.setText(isFinish ? R.string.delete : !isPause ? R.string.start : R.string.resume);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_QUEUE:
                    holder.downloadButton.setText(R.string.queue);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_CONNECTING:
                    holder.downloadButton.setText(R.string.connecting);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_DOWNLOADING:
                    holder.downloadButton.setText(R.string.pause);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_PAUSE:
                    holder.downloadButton.setText(R.string.resume);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_FINISH:
                    holder.downloadButton.setText(R.string.delete);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
                case TASK_STATUS_REQUEST_ERROR:
                    holder.downloadButton.setText(R.string.retry);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                case TASK_STATUS_STORAGE_ERROR:
                    holder.downloadButton.setText(R.string.retry);
                    holder.progressBar.setProgress(Integer.parseInt(progress));
                    holder.progressView.setText(progress);
                    break;
            }
        }


        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = entity.getUrl();
                String taskId = String.valueOf(position);


                final PlayChapterDescriptor item = new PlayChapterDescriptor();
                item.chapterId = taskId;
                item.bookId = taskId;
                DownloadTask itemTask = BookDownloadManager.getInstance().getDownloadTask(item);

                if (itemTask == null) {
                    itemTask = new DownloadTask(new TaskEntity.Builder().url(entity.getUrl()).build());
                    responseUIListener(itemTask, holder);
                    BookDownloadManager.getInstance().addTask(itemTask);
                } else {
                    responseUIListener(itemTask, holder);
                    TaskEntity taskEntity = itemTask.getTaskEntity();
                    int status = taskEntity.getTaskStatus();
                    switch (status) {
                        case TASK_STATUS_QUEUE:
                            BookDownloadManager.getInstance().pauseTask(itemTask);
                            break;
                        case TASK_STATUS_INIT:
                            BookDownloadManager.getInstance().addTask(itemTask);
                            break;
                        case TASK_STATUS_CONNECTING:
                            BookDownloadManager.getInstance().pauseTask(itemTask);
                            break;
                        case TASK_STATUS_DOWNLOADING:
                            BookDownloadManager.getInstance().pauseTask(itemTask);
                            break;
                        case TASK_STATUS_CANCEL:
                            BookDownloadManager.getInstance().addTask(itemTask);
                            break;
                        case TASK_STATUS_PAUSE:
                            BookDownloadManager.getInstance().resumeTask(itemTask);
                            break;
                        case TASK_STATUS_FINISH:
                            BookDownloadManager.getInstance().cancelTask(itemTask);
                            break;
                        case TASK_STATUS_REQUEST_ERROR:
                            BookDownloadManager.getInstance().addTask(itemTask);
                            break;
                        case TASK_STATUS_STORAGE_ERROR:
                            BookDownloadManager.getInstance().addTask(itemTask);
                            break;
                    }
                }
            }
        });
    }


    private void responseUIListener(@NonNull final DownloadTask itemTask, final CViewHolder holder) {

        final TaskEntity taskEntity = itemTask.getTaskEntity();

        itemTask.setListener(new DownloadTaskListener() {

            @Override
            public void onQueue(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.queue);
                }
            }

            @Override
            public void onConnecting(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.connecting);
                }
            }

            @Override
            public void onStart(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.pause);
                    holder.progressBar.setProgress(Integer.parseInt(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize())));
                    holder.progressView.setText(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize()));
                }
            }

            @Override
            public void onPause(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.resume);
                }
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.start);
                    holder.progressView.setText("0");
                    holder.progressBar.setProgress(0);
                }
            }

            @Override
            public void onFinish(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.delete);
                }
            }

            @Override
            public void onError(DownloadTask downloadTask, int codeError) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {

                    holder.downloadButton.setText(R.string.retry);
                    switch (codeError) {
                        case TASK_STATUS_REQUEST_ERROR:
                            Toast.makeText(mContext, R.string.request_error, Toast.LENGTH_SHORT).show();
                            break;
                        case TASK_STATUS_STORAGE_ERROR:
                            Toast.makeText(mContext, R.string.storage_error, Toast.LENGTH_SHORT).show();
                            break;

                    }

                }
            }
        });

    }

    private String getPercent(long completed, long total) {

        if (total > 0) {
            double fen = ((double) completed / (double) total) * 100;
            DecimalFormat df1 = new DecimalFormat("0");
            return df1.format(fen);
        }
        return "0";
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class CViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_title)
        TextView titleView;

        @BindView(R.id.list_item_progress_bar)
        ProgressBar progressBar;

        @BindView(R.id.list_item_progress_text)
        TextView progressView;

        @BindView(R.id.list_item_state_button)
        Button downloadButton;

        CViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
