package me.bigteddy98.bannerboard.api;

import java.util.List;

import org.bukkit.entity.Player;

public abstract class InteractHandler<T> extends BannerBoardRenderer<T> {

	public InteractHandler(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);
	}

	public abstract void handle(Action action, Player clicker, int boardWidth, int boardHeight, int frameIndex, int bannerId);
}
