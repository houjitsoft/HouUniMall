#!/bin/bash

# Maven 仓库上传脚本
# 作者: HouCloud Team
# 日期: 2025-09-05

set -e

echo "🚀 开始上传 HouCloud Commons 到 Maven 仓库..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查必要的工具
check_requirements() {
    print_step "检查必要工具..."
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装，请先安装 Maven"
        exit 1
    fi
    
    if ! command -v gpg &> /dev/null; then
        print_warning "GPG 未安装，将跳过签名步骤"
        SKIP_GPG=true
    fi
    
    print_message "工具检查完成"
}

# 清理和编译
clean_and_compile() {
    print_step "清理和编译项目..."
    
    mvn clean compile
    
    if [ $? -eq 0 ]; then
        print_message "编译成功"
    else
        print_error "编译失败"
        exit 1
    fi
}

# 运行测试
run_tests() {
    print_step "运行测试..."
    
    mvn test
    
    if [ $? -eq 0 ]; then
        print_message "测试通过"
    else
        print_warning "测试失败，但继续构建"
    fi
}

# 生成源码和文档
generate_sources_and_docs() {
    print_step "生成源码和文档..."
    
    mvn source:jar javadoc:jar
    
    if [ $? -eq 0 ]; then
        print_message "源码和文档生成成功"
    else
        print_error "源码和文档生成失败"
        exit 1
    fi
}

# 安装到本地仓库
install_to_local() {
    print_step "安装到本地 Maven 仓库..."
    
    mvn install -DskipTests
    
    if [ $? -eq 0 ]; then
        print_message "本地安装成功"
    else
        print_error "本地安装失败"
        exit 1
    fi
}

# 上传到 Maven Central
deploy_to_maven_central() {
    print_step "上传到 Maven Central..."
    
    if [ "$SKIP_GPG" = true ]; then
        print_warning "跳过 GPG 签名，使用 -Dgpg.skip=true"
        mvn clean deploy -Dgpg.skip=true
    else
        mvn clean deploy
    fi
    
    if [ $? -eq 0 ]; then
        print_message "上传到 Maven Central 成功"
    else
        print_error "上传到 Maven Central 失败"
        exit 1
    fi
}

# 上传到私有仓库
deploy_to_private_repo() {
    local repo_url=$1
    local repo_id=$2
    
    print_step "上传到私有仓库: $repo_url"
    
    mvn deploy:deploy-file \
        -DgroupId=com.houjiang \
        -DartifactId=commons \
        -Dversion=1.0.0 \
        -Dpackaging=jar \
        -Dfile=target/commons-1.0.0.jar \
        -Dsources=target/commons-1.0.0-sources.jar \
        -Djavadoc=target/commons-1.0.0-javadoc.jar \
        -DrepositoryId=$repo_id \
        -Durl=$repo_url
    
    if [ $? -eq 0 ]; then
        print_message "上传到私有仓库成功"
    else
        print_error "上传到私有仓库失败"
        exit 1
    fi
}

# 创建分发包
create_distribution() {
    print_step "创建分发包..."
    
    mkdir -p dist
    cp target/*.jar dist/
    cp pom.xml dist/
    cp README.md dist/
    cp USAGE_GUIDE.md dist/
    
    tar -czf commons-1.0.0-dist.tar.gz dist/
    
    print_message "分发包创建完成: commons-1.0.0-dist.tar.gz"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示帮助信息"
    echo "  -l, --local             仅安装到本地仓库"
    echo "  -c, --central           上传到 Maven Central"
    echo "  -p, --private URL ID   上传到私有仓库"
    echo "  -d, --dist              创建分发包"
    echo "  -a, --all               执行所有步骤"
    echo ""
    echo "示例:"
    echo "  $0 --local                    # 仅本地安装"
    echo "  $0 --central                  # 上传到 Maven Central"
    echo "  $0 --private http://repo.example.com/repository/maven-public/ nexus-private"
    echo "  $0 --dist                     # 创建分发包"
    echo "  $0 --all                      # 执行所有步骤"
}

# 主函数
main() {
    case "${1:-}" in
        -h|--help)
            show_help
            exit 0
            ;;
        -l|--local)
            check_requirements
            clean_and_compile
            run_tests
            generate_sources_and_docs
            install_to_local
            ;;
        -c|--central)
            check_requirements
            clean_and_compile
            run_tests
            generate_sources_and_docs
            deploy_to_maven_central
            ;;
        -p|--private)
            if [ -z "$2" ] || [ -z "$3" ]; then
                print_error "私有仓库需要提供 URL 和 Repository ID"
                exit 1
            fi
            check_requirements
            clean_and_compile
            run_tests
            generate_sources_and_docs
            deploy_to_private_repo "$2" "$3"
            ;;
        -d|--dist)
            check_requirements
            clean_and_compile
            generate_sources_and_docs
            create_distribution
            ;;
        -a|--all)
            check_requirements
            clean_and_compile
            run_tests
            generate_sources_and_docs
            install_to_local
            create_distribution
            print_message "所有步骤完成！"
            ;;
        *)
            print_error "未知选项: ${1:-}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
