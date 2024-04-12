<template>
	<view class="create-page">
		<view class="create-step">
			<uni-steps :options="[{title: '商品信息'}, {title: '商品详情'}, {title: '其他设置'}]" :active="currentTab">
			</uni-steps>
		</view>
		<uni-forms ref="form" :modelValue="formValidate" :rules="ruleValidate">
			<uni-row v-show="currentTab === 0">
				商品信息
			</uni-row>
			<uni-row v-show="currentTab === 1">
				商品详情
			</uni-row>
			<uni-row v-show="currentTab === 2">
				其他设置
			</uni-row>
			<uni-forms-item>
				<button v-show="currentTab>0" @click="handleSubmitUp">上一步</button>
				<button v-show="currentTab<2" @click="handleSubmitNest('formValidate')">下一步</button>
				<button v-show="currentTab===2" @click="submit">提交</button>
			</uni-forms-item>
		</uni-forms>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				isDisabled: true,
				currentTab: 0,
				// 表单数据
				formValidate: {
					name: 'LiMing',
					email: 'dcloud@email.com'
				},
				ruleValidate: {
					// 对name字段进行必填验证
					name: {
						rules: [{
								required: true,
								errorMessage: '请输入姓名',
							},
							{
								minLength: 3,
								maxLength: 5,
								errorMessage: '姓名长度在 {minLength} 到 {maxLength} 个字符',
							}
						]
					},
					// 对email字段进行必填验证
					email: {
						rules: [{
							format: 'email',
							errorMessage: '请输入正确的邮箱地址',
						}]
					}
				}
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
			/**
			 * 复写 binddata 方法，如果只是为了校验，无复杂自定义操作，可忽略此方法
			 * @param {String} name 字段名称
			 * @param {String} value 表单域的值
			 */
			// binddata(name,value){
			// 通过 input 事件设置表单指定 name 的值
			//   this.$refs.form.setValue(name, value)
			// },
			// 触发提交表单
			submit() {
				this.$refs.form.validate().then(res => {
					console.log('表单数据信息：', res);
				}).catch(err => {
					console.log('表单错误信息：', err);
				})
			}
		}
	}
</script>
</style>
<style lang="scss">
	.create-step {
		width: 100%;
		padding: 20rpx 0;
		background-color: antiquewhite
	}
</style>
