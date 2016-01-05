package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.viewmodels.ProjectViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(ProjectViewModel.class)
public final class ProjectActivity extends BaseActivity<ProjectViewModel> {
  private ProjectAdapter adapter;

  protected @Bind(R.id.project_recycler_view) RecyclerView projectRecyclerView;
  protected @Bind(R.id.star_fab) FloatingActionButton starFab;
  protected @Bind(R.id.back_project_button) Button backProjectButton;
  protected @Bind(R.id.manage_pledge_button) Button managePledgeButton;
  protected @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  protected @BindColor(R.color.green) int green;
  protected @BindColor(R.color.text_primary) int textPrimary;

  protected @BindDrawable(R.drawable.ic_star_black_24dp) Drawable starDrawable;

  protected @BindString(R.string.project_back_button) String projectBackButtonString;
  protected @BindString(R.string.project_checkout_manage_navbar_title) String managePledgeString;
  protected @BindString(R.string.project_star_confirmation) String projectStarConfirmationString;

  @Inject KSCurrency ksCurrency;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project)); // Project can be null!
    final String param = intent.getStringExtra(getString(R.string.intent_project_param));
    viewModel.initialize(project, param);

    adapter = new ProjectAdapter(viewModel);
    projectRecyclerView.setAdapter(adapter);
    projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  public void show(@NonNull final Project project) {
    adapter.takeProject(project);
    setProjectActionButton(project);
    toggleStarColor(project);
  }

  public void setProjectActionButton(@NonNull final Project project) {
    if (project.isLive()) {
      starFab.setImageDrawable(starDrawable);
      starFab.setVisibility(View.VISIBLE);
    } else {
      starFab.setVisibility(View.GONE);
    }

    if (!project.isBacking() && project.isLive()) {
      backProjectButton.setVisibility(View.VISIBLE);
    } else {
      backProjectButton.setVisibility(View.GONE);
    }

    if (project.isBacking() && project.isLive()) {
      managePledgeButton.setVisibility(View.VISIBLE);
    } else {
      managePledgeButton.setVisibility(View.GONE);
    }

    if (project.isBacking() && !project.isLive()) {
      viewPledgeButton.setVisibility(View.VISIBLE);
    } else {
      viewPledgeButton.setVisibility(View.GONE);
    }
  }

  public void toggleStarColor(@NonNull final Project project) {
    final int starColor = (project.isStarred()) ? green : textPrimary;
    starDrawable.setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    viewModel.takeBackProjectClick();
  }

  @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    viewModel.takeManagePledgeClick();
  }

  @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    viewModel.takeViewPledgeClick();
  }

  public void managePledge(@NonNull final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.editPledgeUrl())
      .putExtra(getString(R.string.intent_toolbar_title), managePledgeString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overrideExitTransition();
  }

  public void overrideExitTransition() {
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnClick(R.id.star_fab)
  public void starProjectClick() {
    viewModel.takeStarClick();
  }

  @OnClick(R.id.share_icon)
  public void shareProject() {
    viewModel.takeShareClick();
  }

  public void showProjectDescription(@NonNull final Project project) {
    startWebViewActivity(project.descriptionUrl());
  }

  public void showCreatorBio(@NonNull final Project project) {
    startWebViewActivity(project.creatorBioUrl());
  }

  public void showUpdates(@NonNull final Project project) {
    startWebViewActivity(project.updatesUrl());
  }

  public void showStarPrompt() {
    ViewUtils.showToast(this, projectStarConfirmationString);
  }

  public void startCheckoutActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.newPledgeUrl())
      .putExtra(getString(R.string.intent_toolbar_title), projectBackButtonString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startCommentsActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, CommentFeedActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startRewardSelectedCheckout(@NonNull final Project project, @NonNull final Reward reward) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_toolbar_title), projectBackButtonString)
      .putExtra(getString(R.string.intent_url), project.rewardSelectedUrl(reward));
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  public void startShareIntent(@NonNull final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType(getString(R.string.intent_share_type))
      .putExtra(Intent.EXTRA_TEXT, String.format("%1$s\r\n\r\n%2$s", project.name(), project.webProjectUrl()));
    startActivity(intent);
  }

  private void startWebViewActivity(@NonNull final String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), url);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true)
      .putExtra(getString(R.string.intent_login_type), LoginToutActivity.REASON_STAR_PROJECT);
    startActivityForResult(intent, ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void startViewPledgeActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ViewPledgeActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startVideoPlayerActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, VideoPlayerActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    viewModel.takeLoginSuccess();
  }
}
