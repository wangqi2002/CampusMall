<template>
	<view class="page">
		<view class="order-category">
			<view class="category-row">
				<view class="row-name">订单类型：</view>
				<view class="row-button">
					<view class='item' :class='orderCategory==0 ? "on": ""' @click="categoryClick(0)">
						<view>配送</view>
					</view>
					<view class='item' :class='orderCategory==2 ? "on": ""' @click="categoryClick(2)">
						<view>回收</view>
					</view>
				</view>
			</view>
			<view class="category-row">
				<view class="row-name">订单状态：</view>
				<view class="row-button">
					<view v-if="orderCategory != 2" style="display: flex;">
						<!-- <view class='item' :class='orderStatus==0 ? "on": ""' @click="statusClick(0)">
							<view>待出库</view>
						</view> -->
						<view class='item' :class='orderStatus==1 ? "on": ""' @click="statusClick(1)">
							<view>待发货</view>
						</view>
						<view class='item' :class='orderStatus==2 ? "on": ""' @click="statusClick(2)">
							<view>待签收</view>
						</view>
					</view>
					<view v-else style="display: flex;">
						<view class='item' :class='orderStatus==7 ? "on": ""' @click="statusClick(7)">
							<view>待回收</view>
						</view>
						<view class='item' :class='orderStatus==8 ? "on": ""' @click="statusClick(8)">
							<view>待入库</view>
						</view>
						<!-- <view class='item' :class='orderStatus==2 ? "on": ""' @click="statusClick(3)">
							<view>待上架</view>
						</view> -->
					</view>
				</view>
			</view>
		</view>
		<view class="order-list">
			<view class='list'>
				<view class='item' v-for="(item,index) in orderList" :key="index">
					<view>
						<view class='title acea-row row-between-wrapper'>
							<view class="acea-row row-middle">
								<text class="sign cart-color acea-row row-center-wrapper"
									v-if="item.activityType !== '普通' && item.activityType !== '核销'">{{item.activityType}}</text>
								<view>{{item.createTime}}</view>
							</view>
							<view class='font-color'>{{item.orderStatus}}</view>
						</view>
						<view class='user-info acea-row row-between-wrapper'>
							<view class="acea-row row-middle">
								<view class='text acea-row row-between'>
									<view class='sign line2'>姓名：</view>
									<view class='info'>
										<view>{{item.realName}}</view>
									</view>
								</view>
								<view class='text acea-row row-between'>
									<view class='sign line2'>电话：</view>
									<view class='info'>
										<view>{{item.userPhone}}</view>
									</view>
								</view>
								<view class='text acea-row row-between'>
									<view class='sign line2'>详细地址：</view>
									<view class='info'>
										<view>{{item.userAddress}}</view>
									</view>
								</view>
							</view>
						</view>
						<view class='item-info acea-row row-between row-top' v-for="(items,index) in item.orderInfoList"
							:key="index">
							<view class='text acea-row row-between'>
								<view class='name line2'>{{items.storeName}}</view>
								<view class='money'>
									<view>x{{items.cartNum}}</view>
								</view>
							</view>
						</view>
						<view class='totalPrice'>共{{item.totalNum}}件商品</view>
					</view>
					<view class='bottom acea-row row-right row-middle'>
						<view class='bnt bg-color' @click='goOrderDetails(item.orderId)'>查看详情</view>
						<view class='bnt bg-color' v-if="orderCategory==2" @click='handleComfirm1(item.orderId)'>确认
						</view>
						<view class='bnt bg-color' v-else @click='handleComfirm(item.orderId)'>确认</view>
					</view>
				</view>
			</view>
			<view class='loadingicon acea-row row-center-wrapper' v-if="orderList.length>0">
				<text class='loading iconfont icon-jiazai' :hidden='loading==false'></text>{{loadTitle}}
			</view>
			<view v-if="orderList.length == 0">
				<emptyPage title="暂无订单~"></emptyPage>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		getOrderAdminList,
	} from '@/api/order.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from "vuex";
	import emptyPage from '@/components/emptyPage.vue'
	export default {
		components: {
			emptyPage,
		},
		data() {
			return {
				loading: false, //是否加载中
				loadend: false, //是否加载完毕
				loadTitle: '加载更多', //提示语
				orderCategory: 0, //订单类别
				orderStatus: 1, //订单状态
				orderList: [],
				loadTitle: '加载更多', //提示语
				page: 1,
				limit: 20,
			}
		},
		computed: mapGetters(['isLogin', 'userInfo']),
		onShow() {
			if (this.isLogin) {
				this.loadend = false;
				this.page = 1;
				this.$set(this, 'orderList', []);
				// this.getOrderData();
				this.getOrderAdminList();
			} else {
				toLogin();
			}
		},
		methods: {
			handleComfirm: function(order_id) {
				console.log("handleComfirm", order_id)
			},
			handleComfirm1: function(order_id) {
				console.log(order_id)
			},
			confirmOrder: function() {
				let that = this;
				uni.showModal({
					title: '确认收货',
					content: '为保障权益，请收到货确认无误后，再确认收货',
					// success: function(res) {
					// 	if (res.confirm) {
					// 		orderTake(that.id).then(res => {
					// 			return that.$util.Tips({
					// 				title: '操作成功',
					// 				icon: 'success'
					// 			}, function() {
					// 				that.getOrderInfo();
					// 			});
					// 		}).catch(err => {
					// 			return that.$util.Tips({
					// 				title: err
					// 			});
					// 		})
					// 	}
					// }
				})
			},
			/**
			 * 去订单详情
			 */
			goOrderDetails: function(order_id) {
				if (!order_id) return that.$util.Tips({
					title: '缺少订单号无法查看订单详情'
				});
				// #ifdef MP
				uni.showLoading({
					title: '正在加载',
				})
				openOrderSubscribe().then(() => {
					uni.hideLoading();
					uni.navigateTo({
						url: '/pages/admin_order_details/index?order_id=' + order_id
					})
				}).catch(() => {
					uni.hideLoading();
				})
				// #endif  
				// #ifndef MP
				uni.navigateTo({
					url: '/pages/admin_order_details/index?order_id=' + order_id
				})
				// #endif
			},
			/**
			 * 切换类别
			 */
			categoryClick: function(category) {
				if (category == this.orderCategory) return;
				if (category == 0) this.orderStatus = 1
				if (category == 2) this.orderStatus = 7
				this.orderCategory = category;
				this.loadend = false;
				this.page = 1;
				this.$set(this, 'orderList', []);
				this.getOrderAdminList();
			},
			/**
			 * 切换状态
			 */
			statusClick: function(status) {
				if (status == this.orderStatus) return;
				this.orderStatus = status;
				this.loadend = false;
				this.page = 1;
				this.$set(this, 'orderList', []);
				this.getOrderAdminList();
			},
			/**
			 * 获取订单列表
			 */
			getOrderAdminList: function() {
				let that = this;
				if (that.loadend) return;
				if (that.loading) return;
				that.loading = true;
				that.loadTitle = "加载更多";
				console.log("category:", that.orderCategory, "type:", that.orderStatus)
				getOrderAdminList({
					category: that.orderCategory,
					type: that.orderStatus,
					page: that.page,
					limit: that.limit,
				}).then(res => {
					console.log(res.data)
					let list = res.data.list || [];
					let loadend = list.length < that.limit;
					that.orderList = that.$util.SplitArray(list, that.orderList);
					that.$set(that, 'orderList', that.orderList);
					that.loadend = loadend;
					that.loading = false;
					that.loadTitle = loadend ? "我也是有底线的" : '加载更多';
					that.page = that.page + 1;
				}).catch(err => {
					that.loading = false;
					that.loadTitle = "加载更多";
				})
			},
		}
	}
</script>
<style scoped>
	page {
		width: 100%;
		height: 100%;
		background-color: #f8f8f8;
	}
</style>
<style lang="scss">
	.page {
		width: 100%;
		height: 100%;
		background-color: #f8f8f8;

		.order-category {
			margin: 10rpx 12rpx;
			padding: 10rpx 0;
			border-radius: 12rpx;
			background-color: #fdfdfd;

			.category-row {
				width: 100%;
				height: 54rpx;
				line-height: 54rpx;
				margin: 15rpx 0;
				display: flex;

				.row-name {
					text-align: center;
					font-size: 30rpx;
					font-weight: bolder;
					color: #282828;
					padding: 0 20rpx 0 30rpx;
				}

				.row-button {
					border-radius: 8rpx;
					border: 1px solid #7e7e7e;
					display: flex;

					.item {
						width: 120rpx;
						text-align: center;
						font-size: 26rpx;
						color: #282828;
						border-right: 1px solid #7e7e7e;
					}

					.item:last-child {
						border: none;
					}

					.item.on {
						background-color: #1890ff;
						color: #fff;
					}
				}
			}
		}

		.order-list {
			.list {
				width: 720rpx;
				margin: 14rpx auto 0 auto;

				.item {
					background-color: #fff;
					border-radius: 14rpx;
					margin-bottom: 14rpx;

					.title {
						height: 84rpx;
						padding: 0 24rpx;
						border-bottom: 1rpx solid #eee;
						font-size: 28rpx;
						color: #282828;

						.sign {
							font-size: 24rpx;
							padding: 0 13rpx;
							height: 36rpx;
							margin-right: 15rpx;
							border-radius: 18rpx;
						}
					}

					.user-info {
						border-bottom: 1rpx solid #eee;
						font-size: 28rpx;
						color: #282828;

						.text {
							width: 100%;
							font-size: 28rpx;
							padding: 0 24rpx;
							margin: 10rpx 0;
							color: #999;

							.sign {
								width: 140rpx;
								color: #282828;
							}

							.info {
								text-align: left;
							}
						}
					}

					.item-info {
						padding: 0 24rpx;
						margin-top: 22rpx;

						.text {
							width: 100%;
							font-size: 28rpx;
							color: #999;

							.name {
								width: 350rpx;
								color: #282828;
							}

							.money {
								text-align: right;
							}
						}
					}

					.totalPrice {
						font-size: 26rpx;
						color: #282828;
						text-align: right;
						margin: 27rpx 0 0 30rpx;
						padding: 0 30rpx 30rpx 0;
						border-bottom: 1rpx solid #eee;

						.money {
							font-size: 28rpx;
							font-weight: bold;
						}
					}

					.bottom {
						height: 107rpx;
						padding: 0 30rpx;

						.bnt {
							width: 176rpx;
							height: 60rpx;
							margin: 0 10rpx;
							text-align: center;
							line-height: 60rpx;
							color: #fff;
							border-radius: 50rpx;
							font-size: 27rpx;
						}
					}
				}
			}
		}
	}
</style>
