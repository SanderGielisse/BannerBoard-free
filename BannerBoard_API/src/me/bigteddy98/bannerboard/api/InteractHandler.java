package me.bigteddy98.bannerboard.api;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class InteractHandler<T> extends BannerBoardRenderer<T> {

	public InteractHandler(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);
	}

	public abstract void handle(Action action, Player clicker, int boardWidth, int boardHeight, int frameIndex, int bannerId);
}
