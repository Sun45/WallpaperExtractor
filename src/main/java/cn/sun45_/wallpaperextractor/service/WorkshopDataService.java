package cn.sun45_.wallpaperextractor.service;

import cn.sun45_.wallpaperextractor.model.WorkshopListItem;
import cn.sun45_.wallpaperextractor.monitor.WorkshopData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.function.Consumer;

/**
 * Workshop数据服务类
 *
 * <p>负责管理Workshop项目的增删改查和状态管理，与UI控制器解耦</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>管理Workshop项目列表和映射表</li>
 *   <li>处理项目数据的更新和同步</li>
 *   <li>提供项目状态管理功能</li>
 *   <li>支持数据变化监听</li>
 * </ul>
 */
public class WorkshopDataService {
    private final ObservableList<WorkshopListItem> workshopItemsList;
    private final Map<String, WorkshopListItem> workshopItemsMap;
    private Consumer<Integer> onDataChangedListener;

    /**
     * 构造函数
     */
    public WorkshopDataService() {
        this.workshopItemsList = FXCollections.observableArrayList();
        this.workshopItemsMap = new HashMap<>();
    }

    /**
     * 设置数据变化监听器
     *
     * @param listener 数据变化监听器，接收项目数量参数
     */
    public void setOnDataChangedListener(Consumer<Integer> listener) {
        this.onDataChangedListener = listener;
    }

    /**
     * 获取Workshop项目列表
     *
     * @return Workshop项目ObservableList
     */
    public ObservableList<WorkshopListItem> getWorkshopItemsList() {
        return workshopItemsList;
    }

    /**
     * 获取Workshop项目映射表
     *
     * @return Workshop项目映射表
     */
    public Map<String, WorkshopListItem> getWorkshopItemsMap() {
        return workshopItemsMap;
    }

    /**
     * 更新Workshop项目数据
     *
     * @param workshopDataList 新的Workshop数据列表
     */
    public void updateWorkshopItems(List<WorkshopData> workshopDataList) {
        if (workshopDataList == null || workshopDataList.isEmpty()) {
            clearWorkshopItems();
            return;
        }

        // 使用Set来跟踪需要保留的项目ID
        Set<String> currentIds = new HashSet<>();

        for (WorkshopData workshopData : workshopDataList) {
            String itemId = workshopData.getId();
            currentIds.add(itemId);

            WorkshopListItem existingItem = workshopItemsMap.get(itemId);

            if (existingItem != null) {
                // 更新现有项目
                WorkshopData existingModel = existingItem.getWorkshopData();
                existingModel.setTime(workshopData.getTime());
                existingModel.setSubscribed(workshopData.isSubscribed());
            } else {
                // 创建新项目并添加到Map和List
                WorkshopListItem newItem = new WorkshopListItem(workshopData);
                workshopItemsMap.put(itemId, newItem);
                Platform.runLater(() -> workshopItemsList.add(newItem));
            }
        }

        // 移除不再存在的项目
        removeObsoleteItems(currentIds);

        // 通知监听器数据已变化
        if (onDataChangedListener != null) {
            onDataChangedListener.accept(workshopDataList.size());
        }
    }

    /**
     * 移除不再存在的项目
     *
     * <p>根据当前有效的项目ID集合，移除列表中不再存在的项目</p>
     *
     * <p>移除逻辑：</p>
     * <ol>
     *   <li>遍历当前项目列表，找出不在有效ID集合中的项目</li>
     *   <li>将这些项目添加到待移除列表</li>
     *   <li>批量从列表和映射表中移除这些项目</li>
     * </ol>
     *
     * @param currentIds 当前有效的项目ID集合
     */
    private void removeObsoleteItems(Set<String> currentIds) {
        // 创建需要移除的项目列表
        List<WorkshopListItem> itemsToRemove = new ArrayList<>();
        
        for (WorkshopListItem item : workshopItemsList) {
            if (!currentIds.contains(item.getId())) {
                itemsToRemove.add(item);
            }
        }
        
        // 批量移除项目 - 必须在JavaFX应用线程上执行
        if (!itemsToRemove.isEmpty()) {
            Platform.runLater(() -> {
                workshopItemsList.removeAll(itemsToRemove);
                for (WorkshopListItem item : itemsToRemove) {
                    workshopItemsMap.remove(item.getId());
                }
            });
        }
    }

    /**
     * 清空Workshop项目列表
     */
    public void clearWorkshopItems() {
        Platform.runLater(() -> {
            workshopItemsList.clear();
            workshopItemsMap.clear();

            // 通知监听器数据已清空
            if (onDataChangedListener != null) {
                onDataChangedListener.accept(0);
            }
        });
    }

    /**
     * 计算实际会拷贝的项目数量
     *
     * @return 实际会拷贝的项目数量（排除已经拷贝成功的项目）
     */
    public int calculateActualCopyCount() {
        if (workshopItemsList == null || workshopItemsList.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (WorkshopListItem item : workshopItemsList) {
            // 只计算非成功状态的项目（NOT_COPIED, COPYING, FAILED）
            if (item.getCopyStatus() != WorkshopListItem.CopyStatus.SUCCESS) {
                count++;
            }
        }
        return count;
    }

    /**
     * 重置非成功项的拷贝状态
     */
    public void resetNonSuccessCopyStatus() {
        Platform.runLater(() -> {
            for (WorkshopListItem item : workshopItemsList) {
                if (item.getCopyStatus() != WorkshopListItem.CopyStatus.SUCCESS) {
                    item.setCopyStatus(WorkshopListItem.CopyStatus.NOT_COPIED);
                }
            }
        });
    }

    /**
     * 根据ID获取Workshop项目
     *
     * @param id 项目ID
     * @return Workshop项目，如果不存在返回null
     */
    public WorkshopListItem getWorkshopItemById(String id) {
        return workshopItemsMap.get(id);
    }

    /**
     * 获取项目总数
     *
     * @return 项目总数
     */
    public int getTotalCount() {
        return workshopItemsList.size();
    }
}
