const editModal = new bootstrap.Modal(document.getElementById('editFinancialGoalModal'));

document.querySelectorAll('.edit-btn').forEach(button => {
    button.addEventListener('click', function() {
        const financialGoalId = this.dataset.id;
        fetch(`/financialGoals/edit/${financialGoalId}`)
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(data => {
                document.getElementById('financialGoalId').value = data.id;
                document.getElementById('nameEdit').value = data.name;
                document.getElementById('targetAmountEdit').value = data.targetAmount;
                document.getElementById('currentAmountEdit').value = data.currentAmount;
                editModal.show();
            })
            .catch(error => console.error('Error:', error));
    });
});