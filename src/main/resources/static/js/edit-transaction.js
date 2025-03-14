document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.edit-btn').forEach(button => {
        button.addEventListener('click', function () {
            const transactionId = this.dataset.id;

            fetch(`/transactions/edit/${transactionId}`)
                .then(response => {
                    if (!response.ok) throw new Error('Error loading transaction data');
                    return response.json();
                })
                .then(data => {

                    // Filling the common fields
                    document.querySelectorAll('#editExpenseTransactionId, #editIncomeTransactionId, #editGoalTransactionId')
                        .forEach(el => el.value = data.id);
                    document.querySelectorAll('#editAccountExpense, #editAccountIncome, #editAccountGoal').
                    forEach(el => el.value = data.accountId);
                    document.querySelectorAll('#editAmountExpense, #editAmountIncome, #editAmountGoal').
                    forEach(el => el.value = data.amount);
                    document.querySelectorAll('#editExpenseDate, #editIncomeDate, #editGoalDate').
                    forEach(el => el.value = data.date);
                    document.querySelectorAll('#editExpenseDescription, #editIncomeDescription, #editGoalDescription').
                    forEach(el => el.value = data.description);

                    if (data.categoryType === 'EXPENSE') {
                        document.getElementById('editExpenseCategory').value = data.categoryId;
                        document.querySelectorAll('#editTransactionTypeTabs .nav-link')
                            .forEach(link => link.classList.remove('active'));
                        document.querySelector('#editTransactionTypeTabs .nav-link[data-bs-target="#editExpense"]')
                            .classList.add('active');
                        document.querySelectorAll('.tab-pane')
                            .forEach(pane => pane.classList.remove('show', 'active'));
                        document.getElementById('editExpense').classList.add('show', 'active');
                    } else if (data.categoryType === 'INCOME') {
                        document.getElementById('editIncomeCategory').value = data.categoryId;
                        document.querySelectorAll('#editTransactionTypeTabs .nav-link')
                            .forEach(link => link.classList.remove('active'));
                        document.querySelector('#editTransactionTypeTabs .nav-link[data-bs-target="#editIncome"]')
                            .classList.add('active');
                        document.querySelectorAll('.tab-pane')
                            .forEach(pane => pane.classList.remove('show', 'active'));
                        document.getElementById('editIncome').classList.add('show', 'active');
                    } else if (data.categoryType === 'FINANCIAL_GOAL') {
                        document.getElementById('editGoalCategory').value = data.categoryId;
                        document.getElementById('editFinancialGoalName').value = data.financialGoalId;
                        document.querySelectorAll('#editTransactionTypeTabs .nav-link')
                            .forEach(link => link.classList.remove('active'));
                        document.querySelector('#editTransactionTypeTabs .nav-link[data-bs-target="#editGoal"]')
                            .classList.add('active');
                        document.querySelectorAll('.tab-pane')
                            .forEach(pane => pane.classList.remove('show', 'active'));
                        document.getElementById('editGoal').classList.add('show', 'active');
                    }

                    const modal = new bootstrap.Modal(document.getElementById('editTransactionModal'));
                    modal.show();

                })
                .catch(error => {
                    alert('Error loading transaction data');
                    console.error('Error:', error);
                });
        });
    });
});