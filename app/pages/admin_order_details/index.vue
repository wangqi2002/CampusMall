<template>
	<view class="page">
		<view class='order-details'>
			<!-- 给header上与data上加on为退款订单-->
			<view class='header bg-color'>
				<view class='picTxt acea-row row-middle'>
					<view class='pictrue'>
						<image :src="orderInfo.statusPic"></image>
					</view>
					<view class='data'>
						<view class='state'>{{orderInfo.orderStatusMsg}}</view>
						<view>{{orderInfo.createTime}}</view>
					</view>
				</view>
			</view>
			<view class="pad30">
				<view class='nav'>
					<uni-steps v-if="category==2" :options="[{title: '待回收'}, {title: '待入库'}]" :active="status - 8">
					</uni-steps>
					<uni-steps v-else :options="[{title: '待出库'}, {title: '待签收'}]" :active="status - 1">
					</uni-steps>
				</view>
				<view class='address borRadius14'>
					<view class='name'>{{orderInfo.realName}}<text class='phone'>{{orderInfo.userPhone}}</text></view>
					<view>{{orderInfo.userAddress}}</view>
				</view>

				<orderGoods :orderId="order_id" :uniId="uniId" :cartInfo="cartInfo"></orderGoods>
			</view>
			<view class="pad30">
				<view class='wrapper borRadius14'>
					<view class='item acea-row row-between'>
						<view>订单编号：</view>
						<view class='conter acea-row row-middle row-right'>{{orderInfo.orderId}}
							<!-- #ifndef H5 -->
							<text class='copy' @tap='copy'>复制</text>
							<!-- #endif -->
							<!-- #ifdef H5 -->
							<text class='copy copy-data' :data-clipboard-text="orderInfo.orderId">复制</text>
							<!-- #endif -->
						</view>
					</view>
					<view class='item acea-row row-between'>
						<view>下单时间：</view>
						<view class='conter'>{{(orderInfo.createTime || 0)}}</view>
					</view>
					<view class='item acea-row row-between'>
						<view>支付状态：</view>
						<view class='conter' v-if="orderInfo.paid">已支付</view>
						<view class='conter' v-else>未支付</view>
					</view>
					<view class='item acea-row row-between'>
						<view>支付方式：</view>
						<view class='conter'>{{orderInfo.payTypeStr}}</view>
					</view>
					<view class='item acea-row row-between' v-if="orderInfo.mark">
						<view>买家留言：</view>
						<view class='conter'>{{orderInfo.mark}}</view>
					</view>
				</view>
				<view>
					<view class='wrapper borRadius14'>
						<view class='item acea-row row-between'>
							<view>配送方式：</view>
							<view class='conter'>同城送</view>
						</view>
						<view class='item acea-row row-between'>
							<view>配送人姓名：</view>
							<view class='conter'>
								{{userInfo.nickname || ''}}
							</view>
						</view>
						<view class='item acea-row row-between'>
							<view>联系电话：</view>
							<view class='conter acea-row row-middle row-right'>
								{{userInfo.phone || ''}}
							</view>
						</view>
					</view>
				</view>
				<view style='height:120rpx;'></view>
				<view class='footer acea-row row-right row-middle'>
					<view class='bnt cancel' @tap='delOrder'>删除订单</view>
					<view class='bnt bg-color' @tap='handleComfirm'>确认</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		getOrderDetail,
		orderDel,
	} from '@/api/order.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from "vuex";
	import orderGoods from "@/components/orderAdminGoods";
	export default {
		components: {
			orderGoods,
		},
		data() {
			return {
				category: '',
				status: '',
				order_id: '',
				uniId: '',
				isClose: false,
				system_store: {},
				cartInfo: [], //购物车产品
				orderInfo: {
					systemStore: {},
					pstatus: {}
				}, //订单详情
				id: 0, //订单id
				type: 'normal'
			};
		},
		computed: mapGetters(['isLogin', 'chatUrl', 'userInfo']),
		onLoad: function(options) {
			// options.type == undefined || options.type == null ? this.type = 'normal' : this.type = options.type;
			if (!options.status && !options.order_id && !options.uniId) return this.$util.Tips({
				title: '缺少参数'
			}, {
				tab: 3,
				url: 1
			});
			// console.log("category: ", options.category, "status: ", options.status, "order_id: ", options.order_id, " uniId: ", options.uniId)
			this.$set(this, 'order_id', options.order_id);
			this.$set(this, 'category', options.category);
			this.$set(this, 'status', options.status);
		},
		onShow() {
			if (this.isLogin) {
				this.getOrderInfo();
			} else {
				toLogin();
			}
		},
		onHide: function() {
			this.isClose = true;
		},
		methods: {
			handleComfirm: function() {
				console.log(this.status, "--handleComfirm--", this.id)
				let that = this;
				// switch (this.status) {
				// 	case 1:
				// 		uni.showModal({
				// 			title: '确认发货',
				// 			content: '请将商品取出后，再确认发货',
				// 			success: function(res) {
				// 				if (res.confirm) {
				// 					console.log(this.status)
				// 					orderTakeOuts(that.id).then(res => {
				// 						return that.$util.Tips({
				// 							title: '操作成功',
				// 							icon: 'success'
				// 						}, function() {
				// 							that.getOrderAdminList();
				// 						});
				// 					}).catch(err => {
				// 						return that.$util.Tips({
				// 							title: err
				// 						});
				// 					})
				// 				}
				// 			}
				// 		})
				// 		break;
				// 	case 2:
				// 		uni.showModal({
				// 			title: '确认送达',
				// 			content: '请将商品送到买家位置后，再确认送达',
				// 			success: function(res) {
				// 				if (res.confirm) {
				// 					console.log(this.status)
				// 					orderTakeSent(that.id).then(res => {
				// 						return that.$util.Tips({
				// 							title: '操作成功',
				// 							icon: 'success'
				// 						}, function() {
				// 							that.getOrderAdminList();
				// 						});
				// 					}).catch(err => {
				// 						return that.$util.Tips({
				// 							title: err
				// 						});
				// 					})
				// 				}
				// 			}
				// 		})
				// 		break;
				// 	case 8:
				// 		uni.showModal({
				// 			title: '确认回收',
				// 			content: '请将商品从卖家处取回后，再确认回收',
				// 			success: function(res) {
				// 				if (res.confirm) {
				// 					console.log(this.status)
				// 					orderTakeRec(that.id).then(res => {
				// 						return that.$util.Tips({
				// 							title: '操作成功',
				// 							icon: 'success'
				// 						}, function() {
				// 							that.getOrderAdminList();
				// 						});
				// 					}).catch(err => {
				// 						return that.$util.Tips({
				// 							title: err
				// 						});
				// 					})
				// 				}
				// 			}
				// 		})
				// 		break;
				// 	case 9:
				// 		uni.showModal({
				// 			title: '确认入库',
				// 			content: '请将商品放入仓库后，再确认入库',
				// 			success: function(res) {
				// 				if (res.confirm) {
				// 					console.log(this.status)
				// 					orderTakeIns(that.id).then(res => {
				// 						return that.$util.Tips({
				// 							title: '操作成功',
				// 							icon: 'success'
				// 						}, function() {
				// 							that.getOrderAdminList();
				// 						});
				// 					}).catch(err => {
				// 						return that.$util.Tips({
				// 							title: err
				// 						});
				// 					})
				// 				}
				// 			}
				// 		})
				// 		break;
				// 	default:
				// 		console.log("default")
				// }
			},
			/**
			 * 获取订单详细信息
			 * 
			 */
			getOrderInfo: function() {
				let that = this;
				uni.showLoading({
					title: "正在加载中"
				});
				getOrderDetail(that.order_id).then(res => {
					uni.hideLoading();
					console.log(res.data)
					that.$set(that, 'orderInfo', res.data);
					that.$set(that, 'system_store', res.data.systemStore);
					that.$set(that, 'id', res.data.id);
					that.$set(that, 'cartInfo', res.data.orderInfoList);
				}).catch(err => {
					uni.hideLoading();
					that.$util.Tips({
						title: err
					}, '/pages/admin_order/index');
				});
			},

			/**
			 * 
			 * 剪切订单号
			 */
			// #ifndef H5
			copy: function() {
				let that = this;
				uni.setClipboardData({
					data: this.orderInfo.orderId
				});
			},
			// #endif
			/**
			 * 
			 * 删除订单
			 */
			delOrder: function() {
				let that = this;
				console.log(this.id)
				// orderDel(this.id).then(res => {
				// 	return that.$util.Tips({
				// 		title: '删除成功',
				// 		icon: 'success'
				// 	}, {
				// 		tab: 3,
				// 		url: 1
				// 	});
				// }).catch(err => {
				// 	return that.$util.Tips({
				// 		title: err
				// 	});
				// });
			},
		}
	}
</script>

<style lang="scss">
	.page {
		.order-details {
			.header {
				height: 250rpx;
				padding: 0 30rpx;

				.picTxt {
					height: 150rpx;
				}

				.data {
					color: rgba(255, 255, 255, 0.8);
					font-size: 24rpx;
					margin-left: 27rpx;

					.state {
						font-size: 30rpx;
						font-weight: bold;
						color: #fff;
						margin-bottom: 7rpx;
					}

					.time {
						margin-left: 20rpx;
					}
				}

				.pictrue {
					width: 110rpx;
					height: 110rpx;

					image {
						width: 100%;
						height: 100%;
					}
				}
			}

			.nav {
				background-color: #fff;
				font-size: 26rpx;
				color: #282828;
				padding: 27rpx 0;
				width: 100%;
				border-radius: 14rpx;
				margin: -100rpx auto 0 auto;
			}

			.address {
				font-size: 26rpx;
				color: #868686;
				background-color: #fff;
				margin-top: 15rpx;
				padding: 30rpx 24rpx;

				.name {
					font-size: 30rpx;
					color: #282828;
					margin-bottom: 15rpx;

					.phone {
						margin-left: 40rpx;
					}
				}
			}

			.wrapper {
				background-color: #fff;
				margin-top: 12rpx;
				padding: 30rpx 24rpx;

				.item {
					font-size: 28rpx;
					color: #282828;

					.conter {
						color: #868686;
						// width: 490rpx;
						text-align: right;

						.copy {
							font-size: 20rpx;
							color: #333;
							border-radius: 20rpx;
							border: 1rpx solid #666;
							padding: 3rpx 15rpx;
							margin-left: 24rpx;
						}
					}
				}

				.item~.item {
					margin-top: 20rpx;
				}
			}

			.footer {
				width: 100%;
				height: 100rpx;
				position: fixed;
				bottom: 0;
				left: 0;
				background-color: #fff;
				padding: 0 30rpx;
				box-sizing: border-box;

				.bnt {
					width: 158rpx;
					height: 54rpx;
					text-align: center;
					line-height: 54rpx;
					border-radius: 50rpx;
					color: #fff;
					font-size: 27rpx;
				}

				.bnt.cancel {
					color: #aaa;
					border: 1rpx solid #ddd;
				}

				.bnt~.bnt {
					margin-left: 18rpx;
				}
			}
		}
	}
</style>
