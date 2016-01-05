package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Category;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class CategoryPromoViewHolder extends KSViewHolder {
  private Category category;
  private final Context context;
  private final Delegate delegate;

  protected @Bind(R.id.card_view) CardView cardView;
  protected @Bind(R.id.explore_text_view) TextView exploreTextView;
  protected @Bind(R.id.live_projects_text_view) TextView liveProjectsTextView;

  protected @BindString(R.string.category_promo_explore_category) String exploreCategoryString;
  protected @BindString(R.string.category_promo_project_count_live_projects) String countLiveProjectsString;

  @Inject KSString ksString;

  public interface Delegate {
    void categoryPromoClick(CategoryPromoViewHolder viewHolder, Category category);
  }

  public CategoryPromoViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final @NonNull Object datum) {
    category = (Category) datum;

    cardView.setCardBackgroundColor(category.colorWithAlpha());
    final @ColorInt int categoryTextColor = category.overlayTextColor(context);
    exploreTextView.setTextColor(categoryTextColor);
    exploreTextView.setText(ksString.format(exploreCategoryString, "category_name", category.name()));
    liveProjectsTextView.setText(ksString.format(
      countLiveProjectsString,
      "project_count",
      category.projectsCount().toString()
    ));
    liveProjectsTextView.setTextColor(categoryTextColor);
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.categoryPromoClick(this, category);
  }
}
