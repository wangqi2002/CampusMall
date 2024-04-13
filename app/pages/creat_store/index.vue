<template>
	<view class="create-page">
		<view class="create-step">
			<uni-steps :options="[{title: '商品信息'}, {title: '商品详情'}, {title: '其他设置'}]" :active="currentTab">
			</uni-steps>
		</view>
		<uni-forms class="create-form" ref="formValidate" :modelValue="formValidate" :rules="ruleValidate"
			@submit.native.prevent>
			<uni-row class="submission-form" v-show="currentTab === 0">
				<uni-col v-bind="grid2">
					<uni-forms-item class="submission-form-item" label="商品名称：" name="storeName" label-width="100px"
						required>
						<uni-easyinput v-model="formValidate.storeName" maxlength="180" placeholder="请输入商品名称"
							:disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item class="submission-form-item" label="商品分类：" name="cateIds" label-width="100px"
						required>
						<uni-data-picker class="selWidth" v-model="formValidate.cateIds" :localdata="merCateList"
							:map="merCateMap" popup-title="商品分类" placeholder="选择分类" :disabled="isDisabled">
						</uni-data-picker>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item class="submission-form-item" label="商品关键字：" name="keyword" label-width="100px"
						required>
						<uni-easyinput v-model="formValidate.keyword" placeholder="请输入商品关键字" :disabled="isDisabled">
						</uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item class="submission-form-item" label="单位：" prop="unitName" label-width="100px"
						required>
						<uni-easyinput v-model="formValidate.unitName" placeholder="请输入单位" :disabled="isDisabled">
						</uni-easyinput>
					</uni-forms-item>
				</uni-col>
				<uni-col v-bind="grid2">
					<uni-forms-item class="submission-form-item" label="商品简介：" prop="storeInfo" label-width="100px"
						required>
						<uni-easyinput v-model="formValidate.storeInfo" type="textarea" maxlength="255" :rows="3"
							placeholder="请输入商品简介" :disabled="isDisabled"></uni-easyinput>
					</uni-forms-item>
				</uni-col>
			</uni-row>
			<uni-row class="submission-form" v-show="currentTab === 1">
				商品详情
			</uni-row>
			<uni-row class="submission-form" v-show="currentTab === 2">
				其他设置
			</uni-row>
			<uni-forms-item class="submission-options">
				<button class="submission priamry_border" type="primary" v-show="currentTab>0"
					@click="handleSubmitUp">上一步</button>
				<button class="submission" type="primary" v-show="currentTab<2"
					@click="handleSubmitNest('formValidate')">下一步</button>
				<button class="submission" type="primary" v-show="currentTab===2" @click="submit">提交</button>
			</uni-forms-item>
		</uni-forms>
	</view>
</template>

<script>
	const defaultList = [{
			"id": 742,
			"pid": 0,
			"path": "/0/",
			"name": "图书报刊",
			"type": 1,
			"url": "",
			"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/03/27/bfd80d3d77904897bbd634419899d3e2h3igy8s6i7.jpg",
			"status": true,
			"sort": 10,
			"children": [{
					"id": 751,
					"pid": 742,
					"path": "/0/742/",
					"name": "工学",
					"type": 1,
					"url": "",
					"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/04/04/39f71f1bf31a4890bb257721b2b3e7de5266fk9u35.png",
					"status": true,
					"sort": 0
				},
				{
					"id": 752,
					"pid": 742,
					"path": "/0/742/",
					"name": "农学",
					"type": 1,
					"url": "",
					"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/04/04/9489adfaad0048b381d59bcdeb9ca63eywo0crj4nu.png",
					"status": true,
					"sort": 0
				},
				{
					"id": 754,
					"pid": 742,
					"path": "/0/742/",
					"name": "医学",
					"type": 1,
					"url": "",
					"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/04/04/2c34d05521624833864d42677f55e61ab5xfnqiryf.png",
					"status": true,
					"sort": 0
				},
				{
					"id": 756,
					"pid": 742,
					"path": "/0/742/",
					"name": "公共课与文化课",
					"type": 1,
					"url": "",
					"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/04/04/af5e60325d9d4dcea241c495f8fbe980jr5si78hcq.png",
					"status": true,
					"sort": 0
				},
				{
					"id": 757,
					"pid": 742,
					"path": "/0/742/",
					"name": "其他",
					"type": 1,
					"url": "",
					"extra": "http://127.0.0.1:8080/crmebimage/public/store/2024/04/04/dad01b8a9f994c1093fa7d94f9f98e7a4cwcy28l2o.png",
					"status": true,
					"sort": 0
				}
			],
			"label": "图书报刊"
		},
		{
			"id": 281,
			"pid": 0,
			"path": "/0/",
			"name": "户外出行",
			"type": 1,
			"url": "",
			"extra": "http://127.0.0.1:8080/crmebimage/public/maintain/2021/12/25/55014ffb17b74d039b5670298e1047fcyw64lmyzlj.jpg",
			"status": true,
			"sort": 0,
			"children": [{
				"id": 290,
				"pid": 281,
				"path": "/0/281/",
				"name": "骑行配件",
				"type": 1,
				"url": "",
				"extra": "http://127.0.0.1:8080/crmebimage/public/store/2021/12/25/ef98f38ef1cd4083a91fb62b1dd8362cmran1psnmc.png",
				"status": true,
				"sort": 0
			}],
			"label": "户外出行"
		},
		{
			"id": 284,
			"pid": 0,
			"path": "/0/283/",
			"name": "日用文创",
			"type": 1,
			"url": "",
			"extra": "http://127.0.0.1:8080/crmebimage/public/store/2021/12/25/d813cbdd98b148c99885652234ad43b1m1aewwn8wd.jpg",
			"status": true,
			"sort": 0,
			"children": [{
				"id": 302,
				"pid": 284,
				"path": "/0/283/284/",
				"name": "办公文具",
				"type": 1,
				"url": "",
				"extra": "http://127.0.0.1:8080/crmebimage/public/store/2021/12/25/de35d545523448db9f3636e34ca085b29qw11rescg.png",
				"status": true,
				"sort": 0
			}],
			"label": "日用文创"
		}
	]
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
		tempId: '',
		attrValue: [{
			image: '',
			price: 0,
			cost: 0,
			otPrice: 0,
			stock: 0,
			barCode: '',
			weight: 0,
			volume: 0
		}],
		attr: [],
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
				isDisabled: false,
				currentTab: 0,
				merCateMap: {
					text: 'name',
					value: 'id'
				},
				merCateList: [],
				formValidate: Object.assign({}, defaultObj),
				ruleValidate: {
					storeName: [{
						required: true,
						message: '请输入商品名称',
						trigger: 'blur'
					}],
					cateIds: [{
						required: true,
						message: '请选择商品分类',
						trigger: 'change',
						type: 'array',
						min: '1'
					}],
					keyword: [{
						required: true,
						message: '请输入商品关键字',
						trigger: 'blur'
					}],
					unitName: [{
						required: true,
						message: '请输入单位',
						trigger: 'blur'
					}],
					storeInfo: [{
						required: true,
						message: '请输入商品简介',
						trigger: 'blur'
					}],
					tempId: [{
						required: true,
						message: '请选择运费模板',
						trigger: 'change'
					}],
					image: [{
						required: true,
						message: '请上传商品图',
						trigger: 'change'
					}],
					sliderImages: [{
						required: true,
						message: '请上传商品轮播图',
						type: 'array',
						trigger: 'change'
					}],
					specType: [{
						required: true,
						message: '请选择商品规格',
						trigger: 'change'
					}]
				},
				grid2: {
					xl: 12,
					lg: 12,
					md: 12,
					sm: 24,
					xs: 24
				},
			}
		},
		methods: {
			handleSubmitUp() {
				if (this.currentTab-- < 0) this.currentTab = 0;
			},
			handleSubmitNest(name) {
				if (this.currentTab++ > 2) this.currentTab = 0;
				// this.$refs[name].validate((valid) => {
				// 	if (valid) {
				// 		if (this.currentTab++ > 2) this.currentTab = 0;
				// 	} else {
				// 		if (!this.formValidate.store_name || !this.formValidate.cate_id || !this.formValidate
				// 			.keyword ||
				// 			!this.formValidate.unit_name || !this.formValidate.store_info ||
				// 			!this.formValidate.image || !this.formValidate.slider_image) {
				// 			this.$message.warning("请填写完整商品信息！");
				// 		}
				// 	}
				// })
			},
			submit() {
				this.$refs.form.validate().then(res => {
					console.log('表单数据信息：', res);
				}).catch(err => {
					console.log('表单错误信息：', err);
				})
			},
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
			this.merCateList = this.filerMerCateList(defaultList)
			console.log(this.merCateList)
		}
	}
</script>
<style scoped>
	page {
		width: 100%;
		height: 100%;
		background-color: #fff;
	}

	.create-form /deep/ .uni-forms-item {
		margin-bottom: 25px;
	}

	.create-form /deep/ .uni-forms-item__label {
		padding: 0%;
	}
</style>
<style lang="scss">
	.create-page {
		width: 100%;
		height: 99%;
		display: flex;
		flex-direction: column;

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
				height: calc(100% - 140rpx);
				padding: 20rpx 40rpx;
			}

			.submission-options {
				height: 100rpx;

				.submission {
					width: 240rpx;
					height: 60rpx;
					line-height: 60rpx;
					border: none;
					border-radius: 12rpx;
					display: inline-block;
					margin: 20rpx;
					font-family: 'Times New Roman', Times, serif;
				}
			}
		}
	}
</style>
