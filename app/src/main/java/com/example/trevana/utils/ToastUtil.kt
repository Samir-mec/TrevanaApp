object ToastUtil {
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
            view?.setBackgroundColor(context.getColor(R.color.success_green))
            show()
        }
    }

    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
            view?.setBackgroundColor(context.getColor(R.color.error_red))
            show()
        }
    }
}