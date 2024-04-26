<template>
	<view class="create-page">
		<view class="create-step">
			<uni-steps :options="[{title: '商品信息'}, {title: '商品详情'}, {title: '商品介绍'}]" :active="currentTab">
			</uni-steps>
		</view>
		<uni-forms class="create-form" ref="formValidate" :modelValue="formValidate" :rules="ruleValidate"
			@submit.native.prevent>
			<uni-row class="submission-form" v-show="currentTab === 0">
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品名称：" name="storeName" label-width="100px" required>
						<uni-easyinput v-model="formValidate.storeName" maxlength="180" placeholder="请输入商品名称"
							:styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品分类：" name="cateId" label-width="100px" required>
						<uni-data-picker class="selWidth" v-model="formValidate.cateId" :localdata="merCateList"
							:map="merCateMap" popup-title="商品分类" placeholder="选择分类" :disabled="isDisabled">
						</uni-data-picker>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品关键字：" name="keyword" label-width="100px" required>
						<uni-easyinput v-model="formValidate.keyword" placeholder="请输入商品关键字" :styles="inputStyles"
							:disabled="isDisabled">
						</uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="单位：" name="unitName" label-width="100px" required>
						<uni-easyinput v-model="formValidate.unitName" placeholder="请输入单位" :styles="inputStyles"
							:disabled="isDisabled">
						</uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品简介：" name="storeInfo" label-width="100px" required>
						<uni-easyinput v-model="formValidate.storeInfo" type="textarea" maxlength="255" :rows="3"
							placeholder="请输入商品简介" :styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品封面图：" name="image" label-width="100px" required>
						<view @click.stop='uploadpic(0)'>
							<view v-if="formValidate.image" class="pictrue">
								<image class="pictrue" :src='formValidate.image'></image>
							</view>
							<view v-else class="upLoad">
								<uni-icons class="upLoad-icon" type="image" size="40"></uni-icons>
							</view>
						</view>
					</uni-forms-item>
				</uni-col>
			</uni-row>
			<uni-row class="submission-form" v-show="currentTab === 1">
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品售价：" name="['attrValue[0]','price']" label-width="100px" required>
						<uni-easyinput v-model="formValidate.attrValue[0].price" type="number" placeholder="请输入商品售价"
							:styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品成本价：" name="['attrValue[0]','cost']" label-width="100px" required>
						<uni-easyinput v-model="formValidate.attrValue[0].cost" type="number" placeholder="请输入商品成本价"
							:styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品原价：" name="['attrValue[0]','otPrice']" label-width="100px" required>
						<uni-easyinput v-model="formValidate.attrValue[0].otPrice" type="number" placeholder="请输入商品原价："
							:styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品数量：" name="['attrValue[0]','stock']" label-width="100px" required>
						<uni-easyinput v-model="formValidate.attrValue[0].stock" type="number" placeholder="请输入商品数量："
							:styles="inputStyles" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item label="商品轮播图：" name="sliderImages" label-width="100px" required>
						<view class="acea-row">
							<view v-for="(item, index) in formValidate.sliderImages" :key="index" class="pictrue">
								<image :src="item"></image>
								<uni-icons v-if="!isDisabled" class="btndel" type="close" @click="handleRemove(index)"
									size="16">
								</uni-icons>
							</view>
							<view v-if="formValidate.sliderImages.length < 5 && !isDisabled" class="upLoad" @click="">
								<view @click.stop='uploadpic(1)'>
									<view class="upLoad">
										<uni-icons class="upLoad-icon" type="image" size="40"></uni-icons>
									</view>
								</view>
							</view>
						</view>
					</uni-forms-item>
				</uni-col>
			</uni-row>
			<uni-row class="submission-form" v-show="currentTab === 2">
				<view class="wrapper">
					<view class='toolbar' @tap="format" style="height: 80px;overflow-y: auto;">
						<view :class="formats.bold ? 'ql-active' : ''" class="iconfont icon-zitijiacu" data-name="bold">
						</view>
						<view :class="formats.italic ? 'ql-active' : ''" class="iconfont icon-zitixieti"
							data-name="italic">
						</view>
						<view :class="formats.underline ? 'ql-active' : ''" class="iconfont icon-zitixiahuaxian"
							data-name="underline"></view>
						<view :class="formats.strike ? 'ql-active' : ''" class="iconfont icon-zitishanchuxian"
							data-name="strike"></view>
						<!-- #ifndef MP-BAIDU -->
						<view :class="formats.align === 'left' ? 'ql-active' : ''" class="iconfont icon-zuoduiqi"
							data-name="align" data-value="left"></view>
						<!-- #endif -->
						<view :class="formats.align === 'center' ? 'ql-active' : ''" class="iconfont icon-juzhongduiqi"
							data-name="align" data-value="center"></view>
						<view :class="formats.align === 'right' ? 'ql-active' : ''" class="iconfont icon-youduiqi"
							data-name="align" data-value="right"></view>
						<view :class="formats.align === 'justify' ? 'ql-active' : ''" class="iconfont icon-zuoyouduiqi"
							data-name="align" data-value="justify"></view>
						<!-- #ifndef MP-BAIDU -->
						<view :class="formats.lineHeight ? 'ql-active' : ''" class="iconfont icon-line-height"
							data-name="lineHeight" data-value="2"></view>
						<view :class="formats.letterSpacing ? 'ql-active' : ''" class="iconfont icon-Character-Spacing"
							data-name="letterSpacing" data-value="2em"></view>
						<view :class="formats.marginTop ? 'ql-active' : ''" class="iconfont icon-722bianjiqi_duanqianju"
							data-name="marginTop" data-value="20px"></view>
						<view :class="formats.marginBottom ? 'ql-active' : ''"
							class="iconfont icon-723bianjiqi_duanhouju" data-name="marginBottom" data-value="20px">
						</view>
						<!-- #endif -->

						<view class="iconfont icon-clearedformat" @tap="removeFormat"></view>

						<!-- #ifndef MP-BAIDU -->
						<view :class="formats.fontFamily ? 'ql-active' : ''" class="iconfont icon-font"
							data-name="fontFamily" data-value="Pacifico"></view>
						<view :class="formats.fontSize === '24px' ? 'ql-active' : ''" class="iconfont icon-fontsize"
							data-name="fontSize" data-value="24px"></view>
						<!-- #endif -->
						<view :class="formats.color === '#0000ff' ? 'ql-active' : ''" class="iconfont icon-text_color"
							data-name="color" data-value="#0000ff"></view>
						<view :class="formats.backgroundColor === '#00ff00' ? 'ql-active' : ''"
							class="iconfont icon-fontbgcolor" data-name="backgroundColor" data-value="#00ff00"></view>
						<view class="iconfont icon--checklist" data-name="list" data-value="check"></view>
						<view :class="formats.list === 'ordered' ? 'ql-active' : ''" class="iconfont icon-youxupailie"
							data-name="list" data-value="ordered"></view>
						<view :class="formats.list === 'bullet' ? 'ql-active' : ''" class="iconfont icon-wuxupailie"
							data-name="list" data-value="bullet"></view>

						<view class="iconfont icon-undo" @tap="undo"></view>
						<view class="iconfont icon-redo" @tap="redo"></view>

						<view class="iconfont icon-outdent" data-name="indent" data-value="-1"></view>
						<view class="iconfont icon-indent" data-name="indent" data-value="+1"></view>
						<view class="iconfont icon-fengexian" @tap="insertDivider"></view>
						<view class="iconfont icon-charutupian" @tap="insertImage"></view>
						<view :class="formats.header === 1 ? 'ql-active' : ''" class="iconfont icon-format-header-1"
							data-name="header" :data-value="1"></view>
						<view :class="formats.script === 'sub' ? 'ql-active' : ''" class="iconfont icon-zitixiabiao"
							data-name="script" data-value="sub"></view>
						<view :class="formats.script === 'super' ? 'ql-active' : ''" class="iconfont icon-zitishangbiao"
							data-name="script" data-value="super"></view>

						<view class="iconfont icon-shanchu" @tap="clear"></view>

						<view :class="formats.direction === 'rtl' ? 'ql-active' : ''"
							class="iconfont icon-direction-rtl" data-name="direction" data-value="rtl"></view>
					</view>
					<view class="editor-wrapper">
						<editor id="editor" class="ql-container" placeholder="开始输入..." show-img-size show-img-toolbar
							show-img-resize @statuschange="onStatusChange" :read-only="readOnly" @ready="onEditorReady"
							@blur="getCon">
						</editor>
					</view>
				</view>
			</uni-row>
			<uni-forms-item class="submission-options">
				<button class="submission priamry_border" type="primary" v-show="currentTab>0"
					@click="handleSubmitUp">上一步</button>
				<button class="submission" type="primary" v-show="currentTab<2"
					@click="handleSubmitNest('formValidate')">下一步</button>
				<button class="submission" type="primary" v-show="currentTab===2"
					@click="handleSubmit('formValidate')">提交</button>
			</uni-forms-item>
		</uni-forms>
	</view>
</template>

<script>
	import {
		getCategoryListTree,
		productCreateApi
	} from '@/api/store.js';
	import {
		recycleOrderCreate
	} from '@/api/order.js';
	import {
		Debounce
	} from '@/utils/validate'
	const defaultObj = {
		image: '',
		sliderImages: [],
		videoLink: '',
		sliderImage: '',
		storeName: '',
		storeInfo: '',
		keyword: '',
		cateIds: [], // 商品分类id
		cateId: null, // 商品分类id传值
		unitName: '',
		sort: 0,
		giveIntegral: 0,
		ficti: 0,
		isShow: false,
		isBenefit: false,
		isNew: false,
		isGood: false,
		isHot: false,
		isBest: false,
		tempId: 1,
		attrValue: [{
			image: '',
			price: 0,
			cost: 0,
			otPrice: 0,
			stock: 0,
			barCode: '',
			weight: 0,
			volume: 0,
			attrValue: JSON.stringify({
				'规格': '默认'
			})
		}],
		attr: [{
			attrName: "规格",
			attrValues: "默认",
			id: 0
		}],
		selectRule: '',
		isSub: false,
		content: '',
		specType: false,
		id: 0,
		couponIds: [],
		coupons: [],
		activity: ['默认', '秒杀', '砍价', '拼团']
	}
	export default {
		data() {
			return {
				//数据项
				merCateList: [],
				formValidate: Object.assign({}, defaultObj),
				ruleValidate: {
					storeName: {
						rules: [{
							required: true,
							errorMessage: '请输入商品名称',
						}]
					},
					cateId: {
						rules: [{
							required: true,
							errorMessage: '请选择商品分类'
						}]
					},
					keyword: {
						rules: [{
							required: true,
							errorMessage: '请输入商品关键字'
						}]
					},
					unitName: {
						rules: [{
							required: true,
							errorMessage: '请输入单位'
						}]
					},
					storeInfo: {
						rules: [{
							required: true,
							errorMessage: '请输入商品简介'
						}]
					},
					image: {
						rules: [{
							required: true,
							errorMessage: '请上传商品图'
						}]
					},
					sliderImages: {
						rules: [{
							required: true,
							errorMessage: '请上传商品轮播图'
						}]
					}
				},

				//配置项
				isDisabled: false,
				currentTab: 0,
				merCateMap: {
					text: 'name',
					value: 'id'
				},
				grid2: {
					xl: 12,
					lg: 12,
					md: 12,
					sm: 24,
					xs: 24
				},
				inputStyles: {
					color: '#333333',
					borderColor: '#cecdc2'
				},
				readOnly: false,
				formats: {},
			}
		},
		methods: {
			//接口
			getCategorySelect() {
				let that = this;
				getCategoryListTree({
					status: -1,
					type: 1
				}).then(res => {
					res = res.data
					that.merCateList = that.filerMerCateList(res)
					const newArr = []
					res.forEach((value, index) => {
						newArr[index] = value
						if (value.child) newArr[index].child = value.child.filter(item => item.status ===
							true)
					}) // 过滤商品分类设置为隐藏的子分类不出现在树形列表里
					that.merCateList = that.filerMerCateList(newArr)
				}).catch(err => {
					return this.$util.Tips({
						title: err
					});
				});
			},

			//editor编辑框
			getCon() {
				this.editorCtx.getContents({
					success: (res) => {
						this.formValidate.content = res.html
					},
					fail: (err) => {
						console.log(err)
					}
				})
			},
			format(e) {
				let {
					name,
					value
				} = e.target.dataset
				if (!name) return
				this.editorCtx.format(name, value)
			},
			clear() {
				uni.showModal({
					title: '清空编辑器',
					content: '确定清空编辑器全部内容？',
					success: res => {
						if (res.confirm) {
							this.editorCtx.clear({
								success: function(res) {
									console.log("clear success")
								}
							})
						}
					}
				})
			},
			insertDivider() {
				this.editorCtx.insertDivider({
					success: function() {
						console.log('insert divider success')
					}
				})
			},
			insertImage() {
				uni.chooseImage({
					count: 1,
					success: (res) => {
						this.editorCtx.insertImage({
							src: res.tempFilePaths[0],
							alt: '图像',
							success: function() {
								console.log('insert image success')
							}
						})
					}
				})
			},
			undo() {
				this.editorCtx.undo()
			},
			redo() {
				this.editorCtx.redo()
			},
			removeFormat() {
				this.editorCtx.removeFormat()
			},
			onEditorReady() {
				// #ifdef MP-BAIDU
				this.editorCtx = requireDynamicLib('editorLib').createEditorContext('editor');
				// #endif

				// #ifdef APP-PLUS || MP-WEIXIN || H5
				uni.createSelectorQuery().select('#editor').context((res) => {
					this.editorCtx = res.context
				}).exec()
				// #endif
			},
			onStatusChange(e) {
				const formats = e.detail
				this.formats = formats
			},

			//表单操作
			/**
			 * 上传文件
			 * 
			 */
			uploadpic: function(val) {
				let that = this;
				that.$util.uploadImageOne({
					url: 'user/upload/image',
					name: 'multipart',
					model: "content",
					pid: 1
				}, function(res) {
					if (!val) {
						that.formValidate.image = res.data.url;
						that.formValidate.attrValue[0].image = res.data.url;
					}
					that.formValidate.sliderImages.push(res.data.url)
				});
			},
			handleRemove(i) {
				this.formValidate.sliderImages.splice(i, 1)
			},
			handleSubmitUp() {
				if (this.currentTab-- < 0) this.currentTab = 0;
			},
			handleSubmitNest(name) {
				// if (this.currentTab++ > 2) this.currentTab = 0;
				this.$refs[name].validate().then(res => {
					if (this.currentTab++ > 2) this.currentTab = 0;
				}).catch(err => {
					if (!this.formValidate.storeName || !this.formValidate.cateId || !this.formValidate
						.keyword ||
						!this.formValidate.unitName || !this.formValidate.storeInfo ||
						!this.formValidate.image || !this.formValidate.sliderImage) {
						this.$util.Tips({
							title: '请填写完整商品信息！'
						});
					}
				})
			},
			handleSubmit: Debounce(function(name) {
				this.formValidate.cateIds.push(this.formValidate.cateId)
				this.formValidate.sliderImage = JSON.stringify(this.formValidate.sliderImages)
				this.$refs[name].validate().then(res => {
					console.log(this.formValidate)
					productCreateApi(this.formValidate).then(async res => {
						this.$util.Tips({
							title: '新增成功！',
							duration: 2000,
							icon: 'success'
						});
						
						// recycleOrderCreate(data).then(res => {
						// }).catch(err => {
						// 	uni.hideLoading();
						// 	return that.$util.Tips({
						// 		title: err
						// 	});
						// });
						
						setTimeout(() => {
							this.currentTab = 0
							this.formValidate = Object.assign({}, defaultObj)
						}, 500);
					}).catch(res => {
						console.log(res)
					})
				}).catch(err => {
					this.$util.Tips({
						title: '请填写完整商品信息！'
					});
				})
			}),

			//功能函数
			filerMerCateList(treeData) {
				return treeData.map((item) => {
					({
						child: item.children,
						...item
					} = {
						child: item.child,
						...item
					});
					if (!item.children) {
						item.disabled = true
					}
					return item
				})
			},
		},
		mounted() {
			// this.formValidate.sliderImages = []
			this.getCategorySelect()
		}
	}
</script>
<style scoped>
	@import "./editor/editor-icon.css";

	page {
		width: 100%;
		height: 100%;
		background-color: #fafafa;
	}

	.create-form /deep/ .uni-forms-item {
		margin: 5px 0 20px;
	}

	.create-form /deep/ .uni-forms-item__label {
		padding: 0%;
	}

	.create-form /deep/ .input-value-border {
		border-color: #cecdc2;
	}
</style>
<style lang="scss">
	.create-page {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		background-color: #fafafa;

		.create-step {
			width: 100%;
			padding: 50rpx 0 40rpx;
		}

		.create-form {
			width: 100%;
			text-align: center;
			flex: 1;

			.submission-form {
				width: 100%;
				height: calc(100% - 40rpx);
				padding: 20rpx 40rpx;

				.pictrue {
					width: 120rpx;
					height: 120rpx;
					border: 1px dotted rgba(0, 0, 0, 0.1);
					margin: 0 10px 10px 0;
					position: relative;
					cursor: pointer;

					image {
						width: 100%;
						height: 100%;
						border: 1px dotted #cecdc2;
					}
				}

				.upLoad {
					width: 120rpx;
					height: 120rpx;
					border: 1px dotted #cecdc2;
					position: relative;

					.upLoad-icon {
						position: absolute;
						top: 50%;
						left: 50%;
						transform: translate(-50%, -50%);
					}
				}

				.acea-row {
					.btndel {
						position: absolute;
						z-index: 1;
						left: 47px;
						top: -5px;
					}
				}

				.wrapper {
					height: 100%;

					.toolbar {
						background-color: #F5F5F5;
						text-align: left;
						font-family: 'Helvetica Neue', 'Helvetica', 'Arial', sans-serif;
						border: 2px inset #cecdc2;
						box-sizing: border-box;
						border-bottom: 0;
					}

					.iconfont {
						display: inline-block;
						padding: 8px 8px;
						cursor: pointer;
						font-size: 20px;
					}

					.ql-container {
						box-sizing: border-box;
						padding: 12px 15px;
						width: 100%;
						min-height: 30vh;
						height: 100%;
						/* margin-top: 20px; */
						font-size: 16px;
						line-height: 1.5;
					}

					.ql-active {
						color: #06c;
					}

					.editor-wrapper {
						height: calc(100% - 80px - 220rpx);
						background: #fff;
						border: 2px inset #cecdc2;
					}
				}
			}

			.submission-options {
				width: 100%;
				height: 180rpx;
				margin: 0;
				position: fixed;
				bottom: 0rpx;

				.submission {
					width: 240rpx;
					height: 60rpx;
					line-height: 60rpx;
					border: none;
					border-radius: 12rpx;
					display: inline-block;
					margin: 40rpx;
					font-family: 'Times New Roman', Times, serif;
				}
			}
		}
	}
</style>
